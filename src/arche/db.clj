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
  (:require [korma.db :refer [defdb mysql]]
            [environ.core :refer [env]]
            [clj-time.core :as time]
            [clj-time.coerce :refer [to-long to-string to-sql-time from-sql-time]])
  (:import [org.joda.time.format DateTimeFormat]))

;; "The value representing the connection to a mysql database"

(defdb db
  (mysql {:user (env :database-user)
                  :password (env :database-password)
                  :host (env :database-host)
                  :db (env :database-name)}))

(def timestamp-fields
  "Time stamps fields"
  [:created_at :updated_at])

(defn sql-timestamp-now
  "Helper to create a timestamp for 'now' that is suitable for a SQL insert"
  []
  (to-sql-time (to-long (time/now))))

(defn new-record-timestamps
  "Returns both the created_at and updated_at timesstamps for 'now' as
  as hash-map"
  []
  (let [timestamp (sql-timestamp-now)]
    (zipmap timestamp-fields [timestamp timestamp])))

(defn cache-key
  "Duplicates the ROR functionality, where a cache key is created based on
  a string (usually a table name), the record's id, a string representation of the
  records updated_at field; e.g., /discoverable-resources/1-2015-01-01-03304"
  [table-name record]
  (let [formatter (. DateTimeFormat (forPattern  "YMdHmsS9"))
        stamp-string (. formatter (print (to-long (from-sql-time (:updated_at record)))))]
    (format "%s/%d-%s" table-name (:id record) stamp-string)))
