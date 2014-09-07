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
            [environ.core :refer [env]]))

(declare discoverable-resources)

(defdb db records/dbspec)

(defn cache-expiry []
  (if-let [expiry (env :cache-expiry)]
    expiry
    600))

(defn base-uri []
  (if-let [uri (env :base-uri)]
    uri
    (throw (Exception. "Missing base uri environmental variable"))))

(defn app-uri-for [path]
  (format "%s%s" (base-uri) path))

(defentity discoverable-resources
  (pk :id)
  (table :discoverable_resources)
  (database db)
  (entity-fields :resource_name :link_relation :href))

(defn discoverable-resource-first [resource-name]
  (first (select
          discoverable-resources
          (where {:resource_name resource-name}))))

(defresource discoverable-resource-collection []
  :available-media-types ["application/json"]
  :handle-ok (fn [_] (format "Returning All of them")))

(defn discoverable-resource-entity-url [resource-name]
  (app-uri-for (format "%s/%s" "v2/discoverable_resources"
                       (ring/url-encode resource-name))))

(defn discoverable-resource-representation [orm-hash-map]
  (json/generate-string
   (conj
    orm-hash-map
    {media/keyword-links
     (media/self-link-relation (discoverable-resource-entity-url (:resource_name orm-hash-map)))})))

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
                                [(http-helper/cache-control-header-private-age 600)
                                 (http-helper/header-location
                                  (discoverable-resource-entity-url (:resource_name entity)))
                                 (http-helper/header-accept media/hale-media-type)])
                 :body (discoverable-resource-representation entity)})))


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
           (GET "/discoverable_resources/:resource-name" [resource-name]
                (discoverable-resource-entity resource-name))
           (GET "/discoverable_resources/" []
                (discoverable-resource-collection)))
  (route/not-found "Not Found")) ;; TODO - this returns content-type text/html, should be text/plain

(def handler
  (-> wormhole-routes
      wrap-params))
