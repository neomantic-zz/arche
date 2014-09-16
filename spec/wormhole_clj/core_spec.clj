(ns wormhole-clj.core-spec
  (:use wormhole-clj.db
        wormhole-clj.resources.discoverable-resources)
  (:require [clojure.java.jdbc :as jdbc]
            [speclj.core :refer :all]
            [wormhole-clj.app-state :refer :all :as app]
            [wormhole-clj.media :refer :all :as media]
            [wormhole-clj.core :refer :all]
            [ring.mock.request :refer :all :as ring-mock]
            [ring.util.response :only [:get-header] :as ring]
            [clojurewerkz.urly.core :as urly]
            [wormhole-clj.resources.profiles :refer :all :as profile]
            [environ.core :refer [env]]))

(defn wormhole-request [uri & params]
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
      (should-be successful? (wormhole-request (format "%s%s" "/discoverable_resources/" "studies"))))
  (it "should have the correct accept header"
                  (should= media/hal-media-type
                           (get (:headers (wormhole-request (format "%s%s" "/discoverable_resources/" "studies"))) "Accept")))
  (it "returns the correct location header"
                  (should= (format "%s/discoverable_resources/studies" (app/base-uri))
                           (get (:headers (wormhole-request (format "%s%s" "/discoverable_resources/" "studies"))) "Location")))
  (it "returns have the correct accept header"
                  (should= "application/hal+json"
                           (get (:headers (wormhole-request (format "%s%s" "/discoverable_resources/" "studies"))) "Content-Type")))))

(describe
 "routes profiles"
 (it "supports the apls/DiscoverableResources route"
     (should-be successful? (wormhole-request "/alps/DiscoverableResources")))
 (it "should have the correct accept header"
     (should= "application/alps+json"
              (get (:headers (wormhole-request "/alps/DiscoverableResources")) "Accept")))
(it "should have the location header"
     (should= (.toString (.mutatePath (urly/url-like (app/base-uri))  "/alps/DiscoverableResources"))
              (get (:headers (wormhole-request "/alps/DiscoverableResources")) "Location")))
 (it "should have the correct content type header"
     (should= "application/alps+json"
              (get (:headers (wormhole-request "/alps/DiscoverableResources")) "Content-Type"))))

(let [response (wormhole-routes
                (header (ring-mock/request :get "/alps/DIscoverableResources")
                        "Accept" "application/x-yaml"))
      actual-status (:status response)
      actual-body (:body response)]
  (describe
   "profile routes with failed accept type"
   (it "returns 406"
       (should= 406 actual-status))
   (it "returns the correct body"
       (should= "Unsupported media-type. Supported media type listed in Accept header." actual-body))
   (it "returns the correct accept type in the response header"
       (should= "application/alps+json" (ring/get-header response "Accept")))))

(run-specs)
