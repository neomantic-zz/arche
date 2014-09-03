(ns wormhole-clj.db
  (:use compojure.core)
  (:require [environ.core :refer [env]]))

(def dbspec {:classname "com.mysql.jdbc.Driver"
             :subprotocol "mysql"
             :user (env :database-user)
             :password (env :database-password)
             :delimiters "`"
             :subname (env :database-subname)})
