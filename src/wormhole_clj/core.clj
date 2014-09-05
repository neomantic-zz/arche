(ns wormhole-clj.core
  (:use compojure.core korma.db korma.core wormhole-clj.db)
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.route :as route]
            [clojure.string :as str]
            [environ.core :refer [env]]))

(declare discoverable-resources)

(defdb db dbspec)

(defentity discoverable-resources
  (pk :id)
  (table :discoverable_resources)
  (database db)
  (entity-fields [:resource_name :resource-name]))

(defresource discoverable-resource-get-collection []
  :available-media-types ["application/json"]
  :handle-ok (fn [_] (format "Returning All of them")))

(defresource discoverable-resource-get [resource-name]
  :available-media-types ["application/vnd.hale+json"]
  :handle-ok (fn [_] (format "Returning %s" resource-name)))

(defn discoverable-resource-create [resource-name link-relation href]
  (let [existing (select
                  discoverable-resources
                  (fields :href [:link_relation :link-relation])
                  (where {:resource_name resource-name}))]
    (if (not-empty existing)
      {:errors
       {:taken-by (first existing)}}
      (let [to-add {:resource_name resource-name :link_relation link-relation :href href}]
        {:id (:generated_key (insert discoverable-resources (values to-add)))
         :resource-name resource-name
         :link-relation link-relation
         :href href}))))

(defroutes wormhole-app
  (context "/v2" []
           (GET "/discoverable_resources/:resource-name" [resource-name]
                (discoverable-resource-get resource-name))
           (GET "/discoverable_resources/" []
                (discoverable-resource-get-collection)))
  (route/not-found "Not Found")) ;; TODO - this returns content-type text/html, should be text/plain

(def handler
  (-> wormhole-app
      wrap-params))
