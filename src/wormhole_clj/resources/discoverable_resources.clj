(ns wormhole-clj.resources.discoverable-resources
  (:use korma.db
        korma.core)
  (:require [cheshire.core :refer :all :as json]
            [liberator.core :refer [resource defresource]]
            [liberator.representation :as rep :refer [ring-response as-response]]
            [wormhole-clj.alps :as alps]
            [wormhole-clj.media :as media]
            [wormhole-clj.db :as records]
            [ring.util.codec :only [:url-encode] :as ring]
            [wormhole-clj.app-state :as app]
            [wormhole-clj.http :as http-helper]
            [pandect.core :refer :all :as digest]
            [inflections.core :refer :all :as inflect]
            [wormhole-clj.resources.profiles :as profile]))

(def ^{:private true} base-name "discoverable_resources")

(def names {:titleized (inflect/camel-case base-name)
            :routable base-name
            :tableized (keyword base-name)
            :singular (inflect/singular base-name)
            :keyword (keyword (inflect/dasherize base-name))})

(defn etag-make [entity-type record]
  (digest/md5 (format "%s/%d-%d" (name entity-type) (:id record)
                      (:updated_at record))))

(defdb db records/dbspec)

(defentity discoverable-resources
  (pk :id)
  (table (:tableized names))
  (database db)
  (entity-fields :resource_name :link_relation :href))

(defn discoverable-resource-entity-url [resource-name]
  (app/app-uri-for (format "/%s/%s" (:routable names) resource-name)))

(defn discoverable-resource-representation [representable-hash-map]
  (json/generate-string
   (conj
    representable-hash-map
    {media/keyword-links
     (conj
      (media/profile-link-relation (app/alps-profile-url (:titleized names)))
      (media/self-link-relation (discoverable-resource-entity-url (:resource_name representable-hash-map))))})))


(defn discoverable-resource-alps-representation []
  (let [link-relation "link_relation"
        href "href"
        singular (inflect/singular (:titleized names))
        resource-name "resource_name"
        base-descriptors [(alps/descriptor-semantic
                           (alps/id link-relation)
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

(defn discoverable-resource-create [resource-name link-relation href]
  (if-let [existing (discoverable-resource-first resource-name)]
    {:errors {:taken-by existing}}
    (let [attributes (conj
                      (records/new-record-timestamps)
                      {:resource_name resource-name
                       :link_relation link-relation
                       :href href})]
      (conj attributes (insert discoverable-resources (values attributes))))))

(defresource discoverable-resource-entity [resource-name]
  :available-media-types [media/hal-media-type]
  :allowed-methods [:get]
  :exists? (fn [_]
             (if-let [existing (discoverable-resource-first resource-name)]
               {::existing existing}
               false))
  :handle-ok (fn [{entity ::existing}]
               (ring-response
                {:status 200
                 :headers (into {}
                                [(http-helper/header-etag (etag-make base-name entity))
                                 (http-helper/cache-control-header-private-age (app/cache-expiry))
                                 (http-helper/header-location
                                  (discoverable-resource-entity-url (:resource_name entity)))
                                 (http-helper/header-accept media/hal-media-type)])
                 :body (discoverable-resource-representation entity)})))

(profile/profile-register!
 {(:keyword names) discoverable-resource-alps-representation})
