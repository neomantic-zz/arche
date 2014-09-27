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
            [compojure.core :refer [defroutes GET]]
            [compojure.handler :refer [api]]
            [compojure.route :as route]
            [arche.resources.discoverable-resources :only (names discoverable-resource-entity) :as discover]
            [arche.resources.entry-points :only (entry-points route) :as entry]
            [arche.resources.profiles :as profile]
            [arche.app-state :as app]
            [ring.adapter.jetty :as jetty]
            [environ.core :refer [env]]
            [inflections.core :refer :all :as inflect]))

(defroutes app
  (GET (format "/%s/:resource-name" app/alps-path) [resource-name]
       (profile/alps-profiles (inflect/hyphenate resource-name)))
  (GET (format "/%s/:resource-name" (:routable discover/names))  [resource-name]
       (discover/discoverable-resource-entity resource-name))
  (GET entry/route [] (entry/entry-points))
  (route/not-found "Not Found"))


(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (jetty/run-jetty (api #'app) {:port port :join? false})))
