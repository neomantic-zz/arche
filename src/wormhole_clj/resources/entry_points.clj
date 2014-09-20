(ns wormhole-clj.resources.entry-points
  (:require  [liberator.core :refer [resource defresource]]
             [wormhole-clj.resources.discoverable-resources :only (discoverable-resources-all) :as d]
             [wormhole-clj.media :as media]
             [wormhole-clj.http :as http-helper]
             [wormhole-clj.app-state :as app]
             [cheshire.core :refer :all :as json]
             [clojurewerkz.urly.core :as urly]
             [inflections.core :only (dasherize) :as inflect]
             [wormhole-clj.resources.profiles :as profile]
             [liberator.representation :as rep :refer [ring-response as-response]]))

(def names
  (let [base-name "entry_points"]
    {:titleized "EntryPoints"
     :alps-type base-name
     :keyword (keyword (inflect/dasherize base-name))}))

(def route "/")

(defn profile-url []
  (app/alps-profile-url (:titleized names)))

(defn type-url []
  (format "%s#%s" (profile-url) (:alps-type names)))

(defn self-url []
  (.toString (.mutatePath
              (urly/url-like (app/base-uri))
              route)))

(defn entry-points-map []
  (let [discoverable-resources (d/discoverable-resources-all)]
    {media/keyword-links
     (apply conj
            (concat
             [(media/profile-link-relation (profile-url))
              (media/type-link-relation (type-url))
              (media/self-link-relation (self-url))]
             (map (fn [discoverable]
                    {(keyword (:resource_name discoverable))
                     {media/keyword-href (:href discoverable)}})
                  discoverable-resources)))}))

(defn alps-representation [] "")

(defresource entry-points []
  :available-media-types [media/hal-media-type]
  :allowed-methods [:get]
  :handle-ok (fn [_]
               (let [body (json/generate-string (entry-points-map))]
                 (ring-response
                  {:status 200
                   :headers (into {}
                                  [(http-helper/header-etag (http-helper/body-etag-make body))
                                   (http-helper/header-location (self-url))
                                   (http-helper/cache-control-header-private-age (app/cache-expiry))
                                   (http-helper/header-accept media/hal-media-type)])
                   :body body}))))

(profile/profile-register!
 {(:keyword names) alps-representation})
