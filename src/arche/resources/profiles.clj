;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;;  arche - A hypermedia resource discovery service
;;
;;  https://github.com/neomantic/arche
;;
;;  Copyright:
;;    2014
;;
;;  License:
;;    LGPL: http://www.gnu.org/licenses/lgpl.html
;;    EPL: http://www.eclipse.org/org/documents/epl-v10.php
;;    See the LICENSE file in the project's top-level directory for details.
;;
;;  Authors:
;;    * Chad Albers
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns arche.resources.profiles
  (:require [arche.alps :as alps]
            [cheshire.core :refer :all :as json]
            [liberator.core :refer [resource defresource]]
            [arche.http :as http-helper]
            [arche.app-state :as app]
            [arche.config :refer [cache-expiry]]
            [arche.media :as media]
            [arche.resources.core :refer :all :as generic]
            [inflections.core :only [:camel-case] :as inflect]
            [liberator.representation :as rep :refer [ring-response as-response]]))

(def registered-profiles
  "A atom of hash-map for storing registered profiles"
  (atom {}))

(defn profile-register!
  "Adds a profile to the map of registered profiles"
  [new-profile]
  (swap! registered-profiles
         conj
         new-profile))

(defn registered-profile-get
  "Gets the function associated with a registered profile, keyed by resource name"
  [resource-name]
  (get @registered-profiles resource-name))

(defresource alps-profiles [resource-name]
  :available-media-types [alps/json-media-type media/json-media-type]
  :handle-not-acceptable generic/not-acceptable-response
  :allowed-mehods [:get]
  :exists? (fn [_]
             (if-let [profile-thunk (registered-profile-get (keyword resource-name))]
               {::profile-thunk profile-thunk}
               false))
  :handle-ok (fn [{profile-thunk ::profile-thunk resource :resource}]
               (let [body (json/generate-string
                           (apply profile-thunk []))]
                 (ring-response
                  {:status 200
                   :headers (into {}
                                  [(http-helper/header-etag (http-helper/etag-make body))
                                   (http-helper/cache-control-header-private-age cache-expiry)
                                   (http-helper/header-accept (clojure.string/join "," ((:available-media-types resource))))
                                   (http-helper/header-location (app/alps-profile-url (inflect/camel-case resource-name)))])
                   :body body}))))
