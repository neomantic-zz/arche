(ns wormhole-clj.core
  (:use compojure.core korma.db korma.core wormhole-clj.db)
  (:require [ring.util.codec :only [url-encode] :as ring]
            [liberator.core :refer [resource defresource]]
            [liberator.representation :as rep :refer [ring-response as-response]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.route :as route]
            [clojure.string :as str]
            [cheshire.core :refer :all :as json]
            [environ.core :refer [env]]))

(declare discoverable-resources)

(defdb db dbspec)

(defn link-href-build [path]
  {:href (format "%s%s"(env :base-uri) path)})

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
  (:href (link-href-build
          (format "%s/%s" "v2/discoverable_resources"
                  (ring/url-encode resource-name)))))

(defn discoverable-resource-representation [orm-hash-map]
  (json/generate-string
   (conj
    orm-hash-map
    {:_links
     {:self
      (link-href-build (discoverable-resource-entity-url (:resource_name orm-hash-map)))}})))

(defn location-header-build [url]
  {"Location" url})

(defn accept-header-build [content-type]
  {"Accept" content-type})

(defresource discoverable-resource-entity [resource-name]
  :available-media-types ["application/vnd.hale+json"]
  :allowed-methods [:get]
  :exists? (fn [_]
             (if-let [existing (discoverable-resource-first resource-name)]
               {::existing existing}
               false))
  :handle-ok (fn [{entity ::existing}]
               (ring-response
                {:status 200
                 :headers (conj
                           (accept-header-build "application/vnd.hale+json")
                           (location-header-build
                            (discoverable-resource-entity-url (:resource_name entity))))
                 :body (discoverable-resource-representation entity)})))


(defn discoverable-resource-create [resource-name link-relation href]
  (if-let [existing (discoverable-resource-first resource-name)]
    {:errors {:taken-by existing}}
    (let [attributes {:resource_name resource-name
                      :link_relation link-relation
                      :href href}]
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
