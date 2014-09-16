(ns wormhole-clj.core
  (:use compojure.core)
  (:require [ring.middleware.params :refer [wrap-params]]
            [compojure.route :as route]
            [ wormhole-clj.resources.discoverable-resources :refer :all :as discover]
            [clojure.string :as str]
            [wormhole-clj.app-state :as app]
            [wormhole-clj.resources.profiles :as profile]
            [inflections.core :refer :all :as inflect]))

(defroutes wormhole-routes
  (GET (format "/%s/:resource-name" app/alps-path) [resource-name]
       (profile/alps-profiles (inflect/hyphenate resource-name)))
  (GET (format "/%s/:resource-name" (:routable discover/names))  [resource-name]
       (discover/discoverable-resource-entity resource-name))
  (route/not-found "Not Found"))

(def handler
  (-> wormhole-routes
      wrap-params))
