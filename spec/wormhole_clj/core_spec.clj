(ns wormhole-clj.core-spec
  (:require [speclj.core :refer :all]
            [wormhole-clj.core :refer :all]))

(defn request [uri & params]
  (wormhole-app {:request-method :get :uri uri :params (first params)}))

(defn successful? [response]
  (= (:status response) 200))

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

(run-specs)
