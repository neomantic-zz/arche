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
  "A hash-map containing strings associated with keys that describe
  a discoverable resources for route, table, and titleization, etc."
  (let [base-name "discoverable_resources"]
    {:titleized (inflect/camel-case base-name)
     :routable base-name
     :tableized (keyword base-name)
     :singular (inflect/singular base-name)
     :keyword (keyword (inflect/dasherize base-name))}))

(def required-descriptors
  "A vector of require params for a descriptor.  They match the fields
  in the database table, and the expect field of a JSON POST request."
  [:resource_name :link_relation_url :href])

(defentity discoverable-resources
  "A korma entity linking the identifier with the discoverable_resources table
  an its fields."
  (pk :id)
  (table (:tableized names))
  (database records/db)
  (entity-fields :resource_name :link_relation_url :href :updated_at :id :created_at))

(defn url-for [record]
  "Returns a string representaton the URL for a given discoverable resource record"
  (app/app-uri-for (format "/%s/%s" (:routable names) (:resource_name record))))

(def profile-url
  "Returns the profile links for a discoverable resource"
  (app/alps-profile-url (:titleized names)))

(defn- filter-for-required-fields
  "Retrieves on the required fields for a discoverable resource."
  [representable-map]
  (into {}
        (filter (fn [i]
                  (some #(= (first i) %) required-descriptors)) representable-map)))

(defn hypermedia-map
  "Returns the hash-map for an individual discoverable resource.  It is suitable
  for serialization."
  [representable-map]
  (conj
   (filter-for-required-fields representable-map)
   {media/keyword-links
    (conj
     (media/profile-link-relation profile-url)
     (media/self-link-relation
      (url-for representable-map)))}))

(defn discoverable-resource-alps-representation
  "Returns a hash-map suitable for JSON serialization that describes the
  alps profile of a discoverable resource."
  []
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
  "Retrieves from the database the first record whose resource_name matches
  the parameter."
  (first (select
          discoverable-resources
          (where {:resource_name resource-name}))))

(defn discoverable-resources-all
  "Returns all discoverable resource records"
  []
  (select discoverable-resources
           (order :id :ASC)))

(defn url-valid? [value]
  "Returns a boolean indicating if the submitted value is a valid URI/URL"
  (try
    (let [protocol (url/protocol-of (url/url-like (URI. value)))]
      (or (=  protocol "https") (=  protocol "http")))
    (catch URISyntaxException e
      false)))

(def validate-url
  "A function that validates a URL/URI"
  (validate-format-fn url-valid?))

(def ^:private validate-nonexistence
  "A validate attribute function that tests if there is a discoverable resource
  records with a resource name"
  (validates-attribute :resource_name
                       (validate-uniqueness-fn
                        (fn [resource-name]
                          (or (nil? resource-name)
                              (empty? resource-name)
                              (nil? (discoverable-resource-first resource-name)))))))

(defn validate-uniqueness [attributes]
  "Tests if a record has the same resource name."
  (validate attributes
             [validate-nonexistence]))


(def validate-href
  "A function to test if the href of the hash-map is both present and a valid URL"
  (validates-attribute :href validate-presence validate-url))

(def validate-link-relation
  "A function to test if the link relation url is both present and a valid URL"
  (validates-attribute :link_relation_url validate-presence validate-url))

(def validate-resource-name-present
  "A function to test if the resource name is present."
  (validates-attribute :resource_name validate-presence))

(defn discoverable-resource-create
  "Given the require keys of resource-name, link-relation-url, and href,
  insert a record into the discoverable resources table."
  [{:keys [resource-name link-relation-url href]}]
  (insert
   discoverable-resources
   (values (conj
            (records/new-record-timestamps)
            {:resource_name resource-name
             :link_relation_url link-relation-url
             :href href})))
  ;; ugly work around for the fact that the timestamps on the record
  ;; are a tiny milliseconds off from the timestamps that are persisted.
  ;; in the database. korma problem?
  (discoverable-resource-first resource-name))

(def ^:private available-media-types
  "A vector of acceptable hypermedia Accept types"
  [media/hal-media-type media/hale-media-type media/json-media-type])

(defn etag-for
  "A string suitable for the records etag."
  [record]
  (http-helper/etag-make
   (records/cache-key (name (:tableized names)) record)))

(defn ring-response-json
  "Given a record, and a status code, returns a ring JSON response for an
  individual discoverable resource record."
  [record status-code]
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
  "A liberator resource for a discoverable resource entity."
  :available-media-types available-media-types
  :allowed-methods [:get]
  :exists? (fn [_]
             (if-let [record (discoverable-resource-first resource-name)]
               {::record record}
               false))
  :handle-ok (fn [{record ::record}]
               (ring-response-json record 200)))

;; Registers the discoverable resource profile fn with the profile registry
(profile/profile-register!
 {(:keyword names) discoverable-resource-alps-representation})
