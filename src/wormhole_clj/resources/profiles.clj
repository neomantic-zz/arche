(ns wormhole-clj.resources.profiles
  (:require [wormhole-clj.alps :as alps]
            [cheshire.core :refer :all :as json]
            [liberator.core :refer [resource defresource]]
            [wormhole-clj.http :as http-helper]
            [wormhole-clj.app-state :as app]
            [inflections.core :only [:camel-case] :as inflect]
            [liberator.representation :as rep :refer [ring-response as-response]]))

(def registered-profiles (atom {}))

(defn profile-register! [new-profile]
  (swap! registered-profiles
         conj
         new-profile))

(defn registered-profile-get [resource-name]
  (get @registered-profiles resource-name))

(defresource alps-profiles [resource-name]
  :available-media-types [alps/json-media-type]
  :allowed-mehods [:get]
  :exists? (fn [_]
             (if-let [profile-thunk (registered-profile-get (keyword resource-name))]
               {::profile-thunk profile-thunk}
               false))
  :handle-ok (fn [{profile-thunk ::profile-thunk}]
               (let [body (json/generate-string
                           (apply profile-thunk []))]
                 (ring-response
                  {:status 200
                   :headers (into {}
                                  [(http-helper/header-etag (http-helper/body-etag-make body))
                                   (http-helper/cache-control-header-private-age (app/cache-expiry))
                                   (http-helper/header-accept alps/json-media-type)
                                   (http-helper/header-location (app/alps-profile-url (inflect/camel-case resource-name)))])
                   :body body}))))
