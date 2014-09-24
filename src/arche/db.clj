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

(ns arche.db
  (:require [environ.core :refer [env]]
            [clj-time.core :as time]
            [clj-time.coerce :as coerce]))

(def dbspec {:classname "com.mysql.jdbc.Driver"
             :subprotocol "mysql"
             :user (env :database-user)
             :password (env :database-password)
             :delimiters "`"
             :subname (env :database-subname)})

(defn sql-timestamp-now []
  (coerce/to-sql-time (coerce/to-long (time/now))))

(defn new-record-timestamps []
  (let [timestamp (sql-timestamp-now)]
    {:created_at timestamp
     :updated_at timestamp}))
