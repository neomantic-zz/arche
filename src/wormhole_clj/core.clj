(ns wormhole-clj.core
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes ANY]]))

(defresource discoverable-resource [resource-name]
  :available-media-types ["application/json"]
  :handle-ok (fn [_] (format "Returning %s" resource-name)))

(defroutes app
  (ANY "/v1/discoverable_resources/:resource-name" [resource-name]
       (discoverable-resource resource-name)))

(def handler
  (-> app
      wrap-params))
