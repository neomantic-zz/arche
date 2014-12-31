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

(ns arche.resources.core
  (:require [arche.http :as http-helper]
            [liberator.representation  :refer [ring-response as-response]]))

(defn not-acceptable-response [liberator-ctx]
  (ring-response
   {:status 406
    :headers (http-helper/header-accept
              (clojure.string/join ","
                                   ((get-in liberator-ctx [:resource :available-media-types]))))
    :body "Unsupported media-type. Supported media type listed in Accept header."}))

(defn error-map-make [errors error-messages]
  {:errors
   (into {}
         (map (fn [[attribute error-keys]]
                {attribute
                 (apply vector (map #(get error-messages %) error-keys))})
              errors))})
