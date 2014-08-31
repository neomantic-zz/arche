(ns wormhole-clj.core-spec
  (:require [speclj.core :refer :all]
            [clojure.java.jdbc :as jdbc]
            [wormhole-clj.core :refer :all]
            [environ.core :refer [env]]))

(defn request [uri & params]
  (wormhole-app {:request-method :get :uri uri :params (first params)}))

(defn successful? [response]
  (= (:status response) 200))

(def mysql-dbspec
  {:classname "com.mysql.jdbc.Driver"
   :subprotocol "mysql"
   :user (env :database-user)
   :password  (env :database-password)
   :delimiters "`"
   :subname (env :database-subname)})

(defn- clean-database []
  (jdbc/db-do-commands mysql-dbspec "TRUNCATE TABLE discoverable_resources;"))

(describe
 "routes to GET discoverable resources"
 (it "supports /v2/discoverable_resources/ with a name"
     (should-be successful? (request "/v2/discoverable_resources/hello")))
 (it "supports /v2/discoverable_resources/ without name"
     (should-be successful? (request "/v2/discoverable_resources/"))))

(describe
 "unknown routes response"
 (it "return the correct error message"
     (should= "Not Found" (:body (request "random/path"))))
 (it "return the correct status code"
    (should= 404 (:status (request "random/path")))))

(describe
 "creates a discoverable resource"
 (before (clean-database))
 (it "creates one"
     (should= "studies" (discoverable-resource-create "studies" "http://localhost/studies" "http://localhost/alps/studies"))))

(run-specs)
