(ns wormhole-clj.core
  (:use compojure.core korma.db korma.core wormhole-clj.db)
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.route :as route]
            [clojure.string :as str]
            [cheshire.core :refer :all :as json]
            [environ.core :refer [env]]))

(declare discoverable-resources)

(def ^{:private true} nil-persisted-entity {:link-relation "" :href "" :resource-name ""})
(def persisted-entity (atom nil-persisted-entity))
(defn reset-persisted-entity! []
  (reset! persisted-entity nil-persisted-entity))

(defdb db dbspec)

(defn link-href-build [path]
  {:href (format "%s%s"(env :base_uri) path)})

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

(defn discoverable-resource-representation [orm-hash-map]
  (json/generate-string orm-hash-map)
  ;; {:link_relation "what"
  ;;  :href (:href orm-hash-map)
  ;;  :resource_name (:resource_name orm-hash-map)
  ;;  ;; :_links {:help (link-href-build "help/discoverable_resources")
  ;;  ;;          :self {:href (location-url orm-hash-map)} ;; FIX - URI escape
  ;;  ;;          :type (link-href-build (format "alps/DiscoverableResources#discoverable_resources"))
  ;;  ;;          :profile (link-href-build "alps/DiscoverableResources")
  ;;  ;;          }
  ;;  }
  )

(defresource discoverable-resource-entity [resource-name]
  :available-media-types ["application/vnd.hale+json"]
  :allowed-methods [:get]
  :exists? (fn [_]
             (if-let [existing (discoverable-resource-first resource-name)]
               (reset! persisted-entity existing)
               false))
  :handle-ok (fn [_]
               (try ;; hack, to get the finally form
                 (discoverable-resource-representation @persisted-entity)
                 (finally (reset-persisted-entity!)))))


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
