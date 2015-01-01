;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;;  arche - A hypermedia resource discovery service
;;
;;  https://github.com/neomantic/arche
;;
;;  Copyright:
;;    2015
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


(ns arche.config
  (:require [korma.db :refer [defdb mysql]]
            [environ.core :refer [env]]))

;; "The value representing the connection to a mysql database"
(defdb db
  (mysql {:user (env :database-user)
          :password (env :database-password)
          :host (env :database-host)
          :db (env :database-name)}))

(defn cache-expiry
  "Returns cache-expiry to be returned in responses. If the CACHE_EXPIRY
  environmental variable is set, then this function returns that value.
  Otherwise, it returns the default of 600 (milliseconds)."
   []
  (if-let [expiry (env :cache-expiry)]
    expiry
    600))

(def port (Integer. (or (env :port) 5000)))
