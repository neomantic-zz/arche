(ns wormhole-clj.resources.discoverable-resources
  (:use korma.db
        korma.core)
  (:require [cheshire.core :refer :all :as json]
            [liberator.core :refer [resource defresource]]
            [liberator.representation :as rep :refer [ring-response as-response]]
            [wormhole-clj.alps :as alps]
            [wormhole-clj.media :as media]
            [wormhole-clj.db :as records]
            [wormhole-clj.app-state :as app]
            [wormhole-clj.http :as http-helper]
            [pandect.core :refer :all :as digest]
            [inflections.core :refer :all :as inflect]
            [wormhole-clj.resources.profiles :as profile]))

(defn etag-make [entity-type record]
  (digest/md5 (format "%s/%d-%d" (name entity-type) (:id record)
                      (:updated_at record))))

(defdb db records/dbspec)

(defentity discoverable-resources
  (pk :id)
  (table :discoverable_resources)
  (database db)
  (entity-fields :resource_name :link_relation :href))

(defn discoverable-resource-entity-url [resource-name]
  (app/app-uri-for (format "/discoverable_resources/%s" resource-name)))

(defn discoverable-resource-representation [representable-hash-map]
  (json/generate-string
   (conj
    representable-hash-map
    {media/keyword-links
     (media/self-link-relation (discoverable-resource-entity-url (:resource_name representable-hash-map)))})))

(defn discoverable-resource-alps-representation []
  (let [link-relation "link_relation"
        href "href"
        resource-name "resource_name"
        entity-type "DiscoverableResources"
        singular (inflect/singular entity-type)
        return-type (inflect/underscore (inflect/singular entity-type))]
    (alps/document-hash-map
     {alps/keyword-descriptor
      [{alps/keyword-href alps/schema-url
        alps/keyword-type alps/type-value-semantic
        alps/keyword-id link-relation
        alps/keyword-doc
        {alps/keyword-value (format "The LinkRelation of the %s" singular)}}
       {alps/keyword-href alps/schema-url
        alps/keyword-type alps/type-value-semantic
        alps/keyword-id href
        alps/keyword-doc
        {alps/keyword-value (format "The HREF to the entry point of the %s" singular)}}
       {alps/keyword-href alps/schema-text
        alps/keyword-type alps/type-value-semantic
        alps/keyword-id resource-name
        alps/keyword-doc
        {alps/keyword-value (format "The name of the %s" singular)}}
       {alps/keyword-type alps/type-value-safe
        alps/keyword-rt (inflect/underscore singular)
        alps/keyword-id "show"
        alps/keyword-doc {alps/keyword-value (format "Returns an individual %s" singular)}}
       {alps/keyword-descriptor
        (into []
              (map (fn [prop] {alps/keyword-href prop}) [link-relation href resource-name "show"]))
        alps/keyword-type alps/type-value-semantic
        alps/keyword-id return-type
        alps/keyword-link
        {alps/keyword-href (format "%s#%s" (app/alps-profile-url entity-type) return-type)
         alps/keyword-rel (name media/link-relation-self)}
        alps/keyword-doc {alps/keyword-value "A Resource that can be discovered via an entry point"}}]
      alps/keyword-link
      {alps/keyword-href (app/alps-profile-url entity-type)
       alps/keyword-rel (name media/link-relation-self)}
      alps/keyword-doc
      {alps/keyword-value "Describes the semantics, states and state transitions associated with DiscoverableResources."}})))

(defn discoverable-resource-first [resource-name]
  (first (select
          discoverable-resources
          (where {:resource_name resource-name}))))

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
                                [(http-helper/header-etag (etag-make "discoverable_resources" entity))
                                 (http-helper/cache-control-header-private-age (app/cache-expiry))
                                 (http-helper/header-location
                                  (discoverable-resource-entity-url (:resource_name entity)))
                                 (http-helper/header-accept media/hal-media-type)])
                 :body (discoverable-resource-representation entity)})))

(profile/profile-register!
 {:discoverable-resources discoverable-resource-alps-representation})
