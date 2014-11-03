;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;;  arche - A hypermedia resource discovery service
;;
;;  https://github.com/neomantic/arche
;;
;;  Copyright:
;;    2014
;;
;;  License:
;;    LGPL: http://www.gnu.org/licenses/lgpl.html
;;    EPL: http://www.eclipse.org/org/documents/epl-v10.php
;;    See the LICENSE file in the project's top-level directory for details.
;;
;;  Authors:
;;    * Chad Albers
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns arche.resources.discoverable-resource
  (:use korma.core arche.validations)
  (:require [cheshire.core :refer :all :as json]
            [liberator.core :refer [resource defresource]]
            [liberator.representation :as rep :refer [ring-response as-response]]
            [arche.alps :as alps]
            [arche.media :as media]
            [arche.db :as records]
            [ring.util.codec :only [:url-encode] :as ring]
            [arche.app-state :as app]
            [arche.http :as http-helper]
            [pandect.core :refer :all :as digest]
            [inflections.core :refer :all :as inflect]
            [clojurewerkz.urly.core :as url]
            [arche.resources.profiles :as profile])
  (:refer-clojure :exclude [resolve])
  (:import [java.net URI URISyntaxException]))

(def names
  (let [base-name "discoverable_resources"]
    {:titleized (inflect/camel-case base-name)
     :routable base-name
     :tableized (keyword base-name)
     :singular (inflect/singular base-name)
     :keyword (keyword (inflect/dasherize base-name))}))

(def required-descriptors
  [:resource_name :link_relation_url :href])

(defentity discoverable-resources
  (pk :id)
  (table (:tableized names))
  (database records/db)
  (entity-fields :resource_name :link_relation_url :href :updated_at :id :created_at))

(defn url-for [record]
  (app/app-uri-for (format "/%s/%s" (:routable names) (:resource_name record))))

(def profile-url
  (app/alps-profile-url (:titleized names)))

(defn- filter-for-required-fields [representable-map]
  (into {}
        (filter (fn [i]
                  (some #(= (first i) %) required-descriptors)) representable-map)))

(defn hypermedia-map [representable-map]
  (conj
   (filter-for-required-fields representable-map)
   {media/keyword-links
    (conj
     (media/profile-link-relation profile-url)
     (media/self-link-relation
      (url-for representable-map)))}))

(defn discoverable-resource-alps-representation []
  (let [link-relation-url "link_relation_url"
        href "href"
        singular (inflect/singular (:titleized names))
        resource-name "resource_name"
        base-descriptors [(alps/descriptor-semantic
                           (alps/id link-relation-url)
                           (alps/doc (format "The LinkRelation of the %s" singular))
                           (alps/href (:url alps/schemas)))
                          (alps/descriptor-semantic
                           (alps/id href)
                           (alps/doc (format "The HREF to the entry point of the %s" singular))
                           (alps/href (:url alps/schemas)))
                          (alps/descriptor-semantic
                           (alps/id resource-name)
                           (alps/doc (format "The name of the %s" singular))
                           (alps/href (:text alps/schemas)))
                          (alps/descriptor-safe
                           (alps/id "show")
                           (alps/doc (format "Returns an individual %s" singular))
                           (alps/rt (:singular names)))]]
    (alps/document-hash-map
      (merge
       (alps/descriptor
        (merge
         base-descriptors
         (merge
          (alps/descriptor
           (map (fn [descriptor]
                   {alps/keyword-href (alps/keyword-id descriptor)})
                base-descriptors))
          (alps/descriptor-semantic
           (alps/id (:singular names))
           (alps/doc "A Resource that can be discovered via an entry point")
           (alps/link
            media/link-relation-self
            (format "%s#%s" (app/alps-profile-url (:titleized names)) (ring/url-encode (:singular names))))))))
       (merge
        (alps/link
         media/link-relation-self
         (app/alps-profile-url (:titleized names)))
        (alps/doc
         (format "Describes the semantics, states and state transitions associated with %s." (:titleized names))))))))

(defn discoverable-resource-first [resource-name]
  (first (select
          discoverable-resources
          (where {:resource_name resource-name}))))

(def default-per-page 25)

(defn ^:private select-discoverable-resources [page per-page]
  (select discoverable-resources
           (offset page)
           (limit per-page)
           (order :id :ASC)))

(defn ^:private paginate-fn [fetcher-fn default-per-page]
  (fn paginate
    ([] (paginate 1))
    ([page] (paginate page default-per-page))
    ([page per-page]
       ;; algorithm works like this: get 1 more than the maximum count (peek)
       ;; and if amount returns match, then there is a next-page
       (let [offset (* (dec page) default-per-page)
             limit (inc (if (or (< per-page 0) (> per-page default-per-page))
                          default-per-page
                          per-page))
             records (fetcher-fn offset limit)
             has-prev (if (and (= (count records) 0) (> page 1))
                        false
                        (not (= page 1)))
             has-next (or (> (count records) per-page)  ;;when, I wanted a specific per_page, and there were more
                          (> (count records) default-per-page))]
         {:has-prev has-prev
          :prev-page (if has-prev (dec page) 0)
          :has-next has-next
          :next-page (if has-next (inc page) 0)
          :records (if has-next
                     (drop-last (apply vector records))
                     (apply vector records))}))))

(def discoverable-resources-paginate
  (paginate-fn select-discoverable-resources 25))

(defn discoverable-resources-all []
  (select discoverable-resources
           (order :id :ASC)))

(defn url-valid? [value]
  (try
    (let [protocol (url/protocol-of (url/url-like (URI. value)))]
      (or (=  protocol "https") (=  protocol "http")))
    (catch URISyntaxException e
      false)))

(def validate-url (validate-format-fn url-valid?))

(def ^:private validate-nonexistence
  (validates-attribute :resource_name
                       (validate-uniqueness-fn
                        (fn [resource-name]
                          (or (nil? resource-name)
                              (empty? resource-name)
                              (nil? (discoverable-resource-first resource-name)))))))

(defn validate-uniqueness [attributes]
  (validate attributes
             [validate-nonexistence]))


(def validate-href (validates-attribute :href validate-presence validate-url))
(def validate-link-relation (validates-attribute :link_relation_url validate-presence validate-url))
(def validate-resource-name-present (validates-attribute :resource_name validate-presence))

(defn discoverable-resource-create
  [{:keys [resource-name link-relation-url href]}]
  (insert
   discoverable-resources
   (values (conj
            (records/new-record-timestamps)
            {:resource_name resource-name
             :link_relation_url link-relation-url
             :href href})))
  ;; ugly work around for the fact that the timestamps on the record
  ;; are tiny milliseconds off from the timestamps that are persisted
  ;; korma problem?
  (discoverable-resource-first resource-name))

(def ^:private available-media-types
  [media/hal-media-type media/hale-media-type media/json-media-type])

(defn etag-for [record]
  (http-helper/etag-make
   (records/cache-key (name (:tableized names)) record)))

(defn ring-response-json [record status-code]
  (ring-response
   {:status status-code
    :headers (into {}
                   [(http-helper/cache-control-header-private-age (app/cache-expiry))
                    (http-helper/header-location (url-for record))
                    (http-helper/header-etag (etag-for record))
                    (http-helper/header-accept
                     (clojure.string/join "," available-media-types))])
    :body (json/generate-string
           (hypermedia-map record))}))

(defresource discoverable-resource-entity [resource-name]
  :available-media-types available-media-types
  :allowed-methods [:get]
  :exists? (fn [_]
             (if-let [record (discoverable-resource-first resource-name)]
               {::record record}
               false))
  :handle-ok (fn [{record ::record}]
               (ring-response-json record 200)))

(profile/profile-register!
 {(:keyword names) discoverable-resource-alps-representation})
