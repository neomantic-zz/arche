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
  (:require [compojure.route :refer [not-found]]
            [compojure.core :refer [routes GET ANY]]
            [compojure.handler :refer [api]]
            [arche.resources.discoverable-resource
             :refer [names discoverable-resource-entity]
             :rename {names pretty-names}]
            [arche.resources.discoverable-resources-collection :refer [discoverable-resources-collection]]
            [arche.resources.entry-points :refer [route entry-points] :rename {route entry-points-route}]
            [arche.resources.profiles :refer [alps-profiles]]
            [arche.app-state :refer :all]
            [arche.config :as config :refer [port]]
            [ring.adapter.jetty :refer [run-jetty]]
            [inflections.core :refer [hyphenate]]))

(def handler
  "This handler responds to routes for getting/creating
   discoverable resources, retrieving the list of entry points and
   alps profiles."
  (api
   (routes
    (GET (format "/%s/:resource-name" alps-path) [resource-name]
         (alps-profiles (hyphenate resource-name)))
    (GET (format "/%s/:resource-name" (:routable pretty-names)) [resource-name]
         (discoverable-resource-entity resource-name))
    (GET entry-points-route [] (entry-points))
    (ANY (format "/%s*"(:routable pretty-names)) [request]
         (discoverable-resources-collection request))
    (not-found "Not Found"))))

(defn -main [& [port]]
  "The main method that starts the jetty server.  If the PORT
  environmental variable has been set, then the server will listen to requests
  on that port. Otherwise the default port of 5000 is used."
  (run-jetty handler {:port config/port :join? false}))
