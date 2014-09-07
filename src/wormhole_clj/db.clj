(ns wormhole-clj.db
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
