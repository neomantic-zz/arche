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

(def port
  "Returns the port to listen to incoming requests on.  Defaults to 5000. To
  change the default, set the PORT environmental variable to an integer
  above the reserver range of ports."
  (Integer. (or (env :port) 5000)))

(def base-uri
  "Returns the base uri of the application based on the required BASE_URI
  environmental variable.  An example of a base URI is http://www.example.org.
  All URLs that the app produces are based off this base-uri. This
  function throws an expection if the BASE_URI environmental variable has
  not been set."
  (if-let [uri (env :base-uri)]
    uri
    (throw (Exception. "Missing base uri environmental variable"))))
