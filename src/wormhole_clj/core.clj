(ns wormhole-clj.core
  (:use compojure.core)
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.route :as route]))

(defresource discoverable-resource-collection []
  :available-media-types ["application/json"]
  :handle-ok (fn [_] (format "Returning All of them")))

(defresource discoverable-resource [resource-name]
  :available-media-types ["application/json"]
  :handle-ok (fn [_] (format "Returning %s" resource-name)))

(defroutes wormhole-app
  (context "/v2" []
           (GET "/discoverable_resources/:resource-name" [resource-name]
                (discoverable-resource resource-name))
           (GET "/discoverable_resources/" []
                (discoverable-resource-collection)))
  (route/not-found "Not Found")) ;; TODO - this returns content-type text/html, should be text/plain

(def handler
  (-> wormhole-app
      wrap-params))
