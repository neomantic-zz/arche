(ns wormhole-clj.core-spec
  (:use wormhole-clj.db
        wormhole-clj.resources.discoverable-resources)
  (:require [clojure.java.jdbc :as jdbc]
            [speclj.core :refer :all]
            [wormhole-clj.app-state :refer :all :as app]
            [wormhole-clj.media :refer :all :as media]
            [wormhole-clj.core :refer :all]
            [wormhole-clj.resources.profiles :refer :all :as profile]
            [environ.core :refer [env]]))


(defn request [uri & params]
  (wormhole-routes {:request-method :get :uri uri :params (first params)}))

(defn successful? [response]
  (= (:status response) 200))

(defn clean-database []
  (jdbc/db-do-commands dbspec "TRUNCATE TABLE discoverable_resources;"))

(defn factory-discoverable-resource-create [resource-name]
  (discoverable-resource-create
   resource-name
   (format "%s%s" "http://factory/alps/" resource-name)
   (format "%s%s" "http://factory/" resource-name)))

(describe
 "routes to GET discoverable resources"
 (describe
  "when item exists"
  (before
   (clean-database)
   (factory-discoverable-resource-create "studies"))
  (it "supports /discoverable_resources/ with a name"
      (should-be successful? (request (format "%s%s" "/discoverable_resources/" "studies"))))
  (it "should have the correct accept header"
                  (should= media/hal-media-type
                           (get (:headers (request (format "%s%s" "/discoverable_resources/" "studies"))) "Accept")))
  (it "returns the correct location header"
                  (should= (format "%s/discoverable_resources/studies" (app/base-uri))
                           (get (:headers (request (format "%s%s" "/discoverable_resources/" "studies"))) "Location")))
  (it "returns have the correct accept header"
                  (should= "application/hal+json"
                           (get (:headers (request (format "%s%s" "/discoverable_resources/" "studies"))) "Content-Type")))))

(run-specs)
