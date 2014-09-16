(ns wormhole-clj.core
  (:use compojure.core
        wormhole-clj.resources.discoverable-resources)
  (:require [ring.middleware.params :refer [wrap-params]]
            [compojure.route :as route]
            [clojure.string :as str]
            [wormhole-clj.app-state :as app]
            [wormhole-clj.resources.profiles :as profile]
            [inflections.core :refer :all :as inflect]))

(defroutes wormhole-routes
  (GET (format "/%s/:resource-name" app/alps-path) [resource-name]
       (profile/alps-profiles (inflect/hyphenate resource-name)))
  (GET "/discoverable_resources/:resource-name" [resource-name]
       (discoverable-resource-entity resource-name))
  (route/not-found "Not Found"))

(def handler
  (-> wormhole-routes
      wrap-params))
