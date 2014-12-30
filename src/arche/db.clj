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

(defdb db
  "The value representing the connection to a mysql database"
  (mysql {:user (env :database-user)
                  :password (env :database-password)
                  :host (env :database-host)
                  :db (env :database-name)}))

(def timestamp-fields
  "Time stamps fields"
  [:created_at :updated_at])

(defn sql-timestamp-now []
  "Helper to create a stamp for 'now' that is suitable for SQL value"
  (to-sql-time (to-long (time/now))))

(defn new-record-timestamps []
  "Returns both the created_at and updated_at times stamps for 'now' as
  as hash-map"
  (let [timestamp (sql-timestamp-now)]
    (zipmap timestamp-fields [timestamp timestamp])))

(defn cache-key [table-name record]
  ;; this duplicates the way ROR creates a cache key
  ;; using its :nsec format
  "Duplicates the ROR functionality, where a cache key is created based on
  a string (usually a table name), the record's id, a string representation of the
  records updated_at field; e.g., /discoverable-resources/1-2015-01-01-03304"
  (let [formatter (. DateTimeFormat (forPattern  "YMdHmsS9"))
        stamp-string (. formatter (print (to-long (from-sql-time (:updated_at record)))))]
    (format "%s/%d-%s" table-name (:id record) stamp-string)))
