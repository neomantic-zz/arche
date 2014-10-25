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

(defn discoverable-resources-all []
  (select discoverable-resources))

(defn url-valid? [value]
  (try
    (let [protocol (url/protocol-of (url/url-like (URI. value)))]
      (or (=  protocol "https") (=  protocol "http")))
    (catch URISyntaxException e
      false)))

(def validate-url (validate-format-fn url-valid?))

(defn validate [attributes]
  (apply conj
         (map #(% attributes)
              [(validates-attribute :href validate-presence validate-url)
               (validates-attribute :link_relation_url validate-presence validate-url)
               (validates-attribute :resource_name validate-presence)])))

(defn discoverable-resource-create [resource-name link-relation-url href]
  (if-let [existing (discoverable-resource-first resource-name)]
    {:errors {:taken-by (filter-for-required-fields existing)}}
    (let [attributes (conj
                      (records/new-record-timestamps)
                      {:resource_name resource-name
                       :link_relation_url link-relation-url
                       :href href})]
      (insert discoverable-resources (values attributes))
      ;; ugly work around for the fact that the timestamps on the record
      ;; are tiny milliseconds off from the timestamps that are persisted
      ;; korma problem?
      (first (select
              discoverable-resources
              (where {:resource_name resource-name}))))))

(defn ring-response-json [record status-code]
  (ring-response
   {:status status-code
    :headers (into {}
                   [(http-helper/cache-control-header-private-age (app/cache-expiry))
                    (http-helper/header-location (url-for record))
                    (http-helper/header-accept media/hal-media-type)])
    :body (json/generate-string
           (hypermedia-map record))}))

(defn etag-for [record]
  (http-helper/etag-make
   (records/cache-key (name (:tableized names)) record)))

(defresource discoverable-resource-entity [resource-name]
  :available-media-types [media/hal-media-type]
  :allowed-methods [:get]
  :exists? (fn [_]
             (if-let [record (discoverable-resource-first resource-name)]
               {::record record}
               false))
  :handle-ok (fn [{record ::record}]
               (ring-response-json record 200))
  :etag (fn [{record ::record}]
          (etag-for record)))

(profile/profile-register!
 {(:keyword names) discoverable-resource-alps-representation})
