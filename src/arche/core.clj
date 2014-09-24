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
  (:use compojure.core)
  (:require [ring.middleware.params :refer [wrap-params]]
            [compojure.route :as route]
            [arche.resources.discoverable-resources :only (names discoverable-resource-entity) :as discover]
            [arche.resources.entry-points :only (entry-points route) :as entry]
            [clojure.string :as str]
            [arche.app-state :as app]
            [arche.resources.profiles :as profile]
            [inflections.core :refer :all :as inflect]))

(defroutes app-routes
  (GET (format "/%s/:resource-name" app/alps-path) [resource-name]
       (profile/alps-profiles (inflect/hyphenate resource-name)))
  (GET (format "/%s/:resource-name" (:routable discover/names))  [resource-name]
       (discover/discoverable-resource-entity resource-name))
  (GET entry/route [] (entry/entry-points))
  (route/not-found "Not Found"))

(def handler
  (-> app-routes
      wrap-params))
