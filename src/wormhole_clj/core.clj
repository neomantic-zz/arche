(ns wormhole-clj.core
  (:use compojure.core korma.db korma.core)
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.route :as route]
            [clojure.string :as str]
            [environ.core :refer [env]]))

(defdb devdb {:classname "com.mysql.jdbc.Driver"
			  :subprotocol "mysql"
              :user (env :database-user)
              :password (env :database-password)
              :delimiters "`"
              :subname (env :database-subname)})

(declare discoverable-resources)

(defentity discoverable-resources
  (pk :id)
  (table :discoverable_resources)
  (database devdb)
  (entity-fields :resource_name))

(defresource discoverable-resource-get-collection []
  :available-media-types ["application/json"]
  :handle-ok (fn [_] (format "Returning All of them")))

(defresource discoverable-resource-get [resource-name]
  :available-media-types ["application/json"]
  :handle-ok (fn [_] (format "Returning %s" resource-name)))

(defn discoverable-resource-create [resource-name link-relation href]
  (insert discoverable-resources
          (values {:resource_name resource-name :link_relation link-relation :href href}))
  resource-name)

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
