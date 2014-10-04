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
            [clj-time.coerce :refer [to-long to-string to-sql-time]])
  (:import [org.joda.time.format DateTimeFormat]))

(defdb db (mysql {:user (env :database-user)
                  :password (env :database-password)
                  :host (env :database-host)
                  :db (env :database-name)}))

(def timestamp-fields
  [:created_at :updated_at])

(defn sql-timestamp-now []
  (to-sql-time (to-long (time/now))))

(defn new-record-timestamps []
  (let [timestamp (sql-timestamp-now)]
    (zipmap timestamp-fields [timestamp timestamp])))

(defn cache-key [table-name record]
  ;; this duplicates the way ROR creates a cache key
  ;; using its :nsec format
  (let [formatter (. DateTimeFormat (forPattern  "YMdHmsS9"))
        stamp-string (. formatter (print (to-long (:updated_at record))))]
    (format "%s/%d-%s" table-name (:id record) stamp-string)))
