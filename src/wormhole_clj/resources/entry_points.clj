(ns wormhole-clj.resources.entry-points
  (:use wormhole-clj.resources.discoverable-resources)
  (:require  [liberator.core :refer [resource defresource]]
             [wormhole-clj.media :as media]
             [wormhole-clj.http :as http-helper]
             [wormhole-clj.app-state :as app]
             [liberator.representation :as rep :refer [ring-response as-response]]))

(defn entry-points-representation []
  true)

(defresource entry-points []
  :available-media-types [media/hal-media-type]
  :allowed-methods [:get]
  :handle-ok (fn [_]
               (ring-response
                {:status 200
                 :headers (into {}
                                [(http-helper/cache-control-header-private-age (app/cache-expiry))
                                 (http-helper/header-accept media/hal-media-type)])
                 :body (entry-points-representation)})))
