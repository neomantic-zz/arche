(ns wormhole-clj.core
  (:use compojure.core korma.db korma.core)
  (:require [ring.util.codec :only [url-encode] :as ring]
            [liberator.core :refer [resource defresource]]
            [liberator.representation :as rep :refer [ring-response as-response]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.route :as route]
            [wormhole-clj.db :as records]
            [clojure.string :as str]
            [cheshire.core :refer :all :as json]
            [wormhole-clj.http :as http-helper]
            [wormhole-clj.media :as media]
            [wormhole-clj.alps :as alps]
            [wormhole-clj.app-state :as app]
            [pandect.core :refer :all :as digest]
            [environ.core :refer [env]]))

(declare discoverable-resources)

(defdb db records/dbspec)

(defn entity-etag-make [entity-type record]
  (digest/md5 (format "%s/%d-%d" (name entity-type) (:id record)
                      (:updated_at record))))

(defn body-etag-make [body]
  (digest/md5 body))

(defentity discoverable-resources
  (pk :id)
  (table :discoverable_resources)
  (database db)
  (entity-fields :resource_name :link_relation :href))

(defn discoverable-resource-first [resource-name]
  (first (select
          discoverable-resources
          (where {:resource_name resource-name}))))

(defn discoverable-resource-entity-url [resource-name]
  (app/app-uri-for (format "v2/discoverable_resources/%s"
                           (ring/url-encode resource-name))))

(defn alps-profile-url [resource-name]
  (app/app-uri-for (format "v2/%s/%s" app/alps-path (ring/url-encode resource-name))))

(defn discoverable-resource-representation [representable-hash-map]
  (json/generate-string
   (conj
    representable-hash-map
    {media/keyword-links
     (media/self-link-relation (discoverable-resource-entity-url (:resource_name representable-hash-map)))})))

(defn discoverable-resource-alps-representation []
  (let [link-relation "link_relation"
        href "href"
        resource-name "resource_name"]
    (alps/document-hash-map
     {alps/keyword-descriptor
      [{alps/keyword-href alps/schema-url
        alps/keyword-type alps/type-value-semantic
        alps/keyword-id link-relation
        alps/keyword-doc
        {alps/keyword-value "The LinkRelation of the DiscoverableResource"}}
       {alps/keyword-href alps/schema-url
        alps/keyword-type alps/type-value-semantic
        alps/keyword-id href
        alps/keyword-doc
        {alps/keyword-value "The HREF to the entry point of the DiscoverableResource"}}
       {alps/keyword-href alps/schema-text
        alps/keyword-type alps/type-value-semantic
        alps/keyword-id resource-name
        alps/keyword-doc
        {alps/keyword-value "The name of the DiscoverableResource"}}
       {alps/keyword-type alps/type-value-safe
        alps/keyword-rt "discoverable_resource"
        alps/keyword-id "show"
        alps/keyword-doc {alps/keyword-value "Returns an individual DiscoverableResource"}}
       {alps/keyword-descriptor
        (into []
              (map (fn [prop] {alps/keyword-href prop}) [link-relation href resource-name "show"]))
        alps/keyword-type alps/type-value-semantic
        alps/keyword-id "discoverable_resource"
        alps/keyword-link
        [{alps/keyword-href (format "%s#%s" (alps-profile-url "DiscoverableResources") "discoverable_resource")
          alps/keyword-rel (name media/link-relation-self)}]
        alps/keyword-doc {alps/keyword-value "A Resource that can be discovered via an entry point"}}]
      alps/keyword-link
      [{alps/keyword-href (alps-profile-url "DiscoverableResources")
        alps/keyword-type (name media/link-relation-self)}]
      alps/keyword-doc
      {alps/keyword-value "Describes the semantics, states and state transitions associated with DiscoverableResources."}})))

(defresource discoverable-resource-entity [resource-name]
  :available-media-types [media/hale-media-type]
  :allowed-methods [:get]
  :exists? (fn [_]
             (if-let [existing (discoverable-resource-first resource-name)]
               {::existing existing}
               false))
  :handle-ok (fn [{entity ::existing}]
               (ring-response
                {:status 200
                 :headers (into {}
                                [(http-helper/header-etag (entity-etag-make "discoverable_resources" entity))
                                 (http-helper/cache-control-header-private-age (app/cache-expiry))
                                 (http-helper/header-location
                                  (discoverable-resource-entity-url (:resource_name entity)))
                                 (http-helper/header-accept media/hale-media-type)])
                 :body (discoverable-resource-representation entity)})))

(def supported-profiles #{"DiscoverableResources"})

(defresource alps-profiles [resource-name]
  :available-media-types [alps/json-media-type]
  :allowed-mehods [:get]
  :exists? (fn [_]
             (if (get supported-profiles resource-name) true false))
  :handle-ok (fn [_]
               (let [body (json/generate-string
                                (discoverable-resource-alps-representation))]
                 (ring-response
                  {:status 200
                   :headers (into {}
                                  [(http-helper/header-etag (body-etag-make body))
                                   (http-helper/cache-control-header-private-age (app/cache-expiry))
                                   (http-helper/header-accept alps/json-media-type)
                                   (http-helper/header-location (alps-profile-url resource-name))])
                   :body body}))))


(defn discoverable-resource-create [resource-name link-relation href]
  (if-let [existing (discoverable-resource-first resource-name)]
    {:errors {:taken-by existing}}
    (let [attributes (conj
                      (records/new-record-timestamps)
                      {:resource_name resource-name
                       :link_relation link-relation
                       :href href})]
      (conj attributes (insert discoverable-resources (values attributes))))))

(defroutes wormhole-routes
  (context "/v2" []
           (GET (format "/%s/:resource-name" app/alps-path) [resource-name]
                (alps-profiles resource-name))
           (GET "/discoverable_resources/:resource-name" [resource-name]
                (discoverable-resource-entity resource-name)))
  (route/not-found "Not Found")) ;; TODO - this returns content-type text/html, should be text/plain

(def handler
  (-> wormhole-routes
      wrap-params))
