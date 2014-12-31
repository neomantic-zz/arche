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

(ns arche.core
  (:require [compojure.route :as route]
            [compojure.core :refer [routes GET ANY]]
            [compojure.handler :refer [api]]
            [arche.resources.discoverable-resource
             :only (names discoverable-resource-entity)
             :as entity]
            [arche.resources.discoverable-resources-collection
             :refer [] :as collection]
            [arche.resources.entry-points :only (entry-points route) :as entry]
            [arche.resources.profiles :as profile]
            [arche.app-state :as config]
            [ring.adapter.jetty :as jetty]
            [environ.core :refer [env]]
            [inflections.core :refer :all :as inflect]))

(def handler
  "This handler which responds to routes for getting/creating
   discoverable resources, retrieving the list of entry points and
   alps profiles."
  (api
   (routes
    (GET (format "/%s/:resource-name" config/alps-path) [resource-name]
         (profile/alps-profiles (inflect/hyphenate resource-name)))
    (GET (format "/%s/:resource-name" (:routable entity/names))  [resource-name]
         (entity/discoverable-resource-entity resource-name))
    (GET entry/route [] (entry/entry-points))
    (ANY "/discoverable_resources*" [request]
         (collection/discoverable-resources-collection request))
    (route/not-found "Not Found"))))

(defn -main [& [port]]
  "The main method that starts the jetty server.  If the PORT
  environmental has been set, then the server will listen to requests
  on that port. Otherwise the default port of 5000 is used."
  (let [port (Integer. (or port (env :port) 5000))]
    (jetty/run-jetty handler {:port port :join? false})))
