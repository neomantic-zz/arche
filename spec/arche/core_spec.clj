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

(ns arche.core-spec
  (:use arche.db
        arche.resources.discoverable-resource)
  (:require [clojure.java.jdbc :as jdbc]
            [speclj.core :refer :all]
            [arche.app-state :refer :all :as app]
            [arche.media :refer :all :as media]
            [arche.core :refer :all]
            [ring.mock.request :refer :all :as ring-mock]
            [ring.util.response :only [:get-header] :as ring]
            [clojurewerkz.urly.core :as urly]
            [arche.resources.profiles :refer :all :as profile]
            [environ.core :refer [env]]))

(defn arche-request [uri & params]
  (app {:request-method :get :uri uri :params (first params)}))

(defn successful? [response]
  (= (:status response) 200))

(def dbspec {:classname "com.mysql.jdbc.Driver"
             :subprotocol "mysql"
             :user (env :database-user)
             :password (env :database-password)
             :delimiters "`"
             :subname (format "//%s:3306/%s"
                              (env :database-host)
                              (env :database-name))})

(defn clean-database []
  (jdbc/db-do-commands
   dbspec
   "TRUNCATE TABLE discoverable_resources;"))

(defn factory-discoverable-resource-create [resource-name]
  (discoverable-resource-create
   {:resource-name  resource-name
    :link-relation-url (format "%s%s" "http://factory/alps/" resource-name)
    :href (format "%s%s" "http://factory/" resource-name)}))

(describe
 "routes to GET discoverable resources"
 (describe
  "when item exists"
  (before
   (clean-database)
   (factory-discoverable-resource-create "studies"))
  (it "supports /discoverable_resources/ with a name"
      (should-be successful? (arche-request (format "%s%s" "/discoverable_resources/" "studies"))))))

(describe
 "routes profiles"
 (it "supports the apls/DiscoverableResources route"
     (should-be successful? (arche-request "/alps/DiscoverableResources"))))

(doseq [mime-type ["application/vnd.hale+json" "application/hal+json" "application/json"]]
  (let [response (app
                  (header (ring-mock/request :get "/")
                          "Accept" mime-type))
        actual-status (:status response)]
    (describe
     "entry point routes"
     (it "is successful"
         (should-be successful? response))
     (it "returns the correct content type header"
         (should=
          mime-type
          (ring/get-header response "Content-Type")))
     (it "returns the correct accept header"
         (should=
          "application/hal+json,application/vnd.hale+json,application/json"
          (ring/get-header response "Accept"))))))

(run-specs)
