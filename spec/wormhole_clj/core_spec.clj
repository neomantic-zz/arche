(ns wormhole-clj.core-spec
  (:use wormhole-clj.db)
  (:require [speclj.core :refer :all]
            [clojure.java.jdbc :as jdbc]
            [wormhole-clj.core :refer :all]
            [environ.core :refer [env]]))

(defn request [uri & params]
  (wormhole-routes {:request-method :get :uri uri :params (first params)}))

(defn successful? [response]
  (= (:status response) 200))

(defn- clean-database []
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
  (it "supports /v2/discoverable_resources/ with a name"
      (should-be successful? (request (format "%s%s" "/v2/discoverable_resources/" "studies"))))
  (it "should have the correct accept header"
                  (should= "application/vnd.hale+json"
                           (get (:headers (request (format "%s%s" "/v2/discoverable_resources/" "studies"))) "Accept")))
  (it "returns the correct location header"
                  (should= "http://test.host/v2/discoverable_resources/studies"
                           (get (:headers (request (format "%s%s" "/v2/discoverable_resources/" "studies"))) "Location")))
  (it "returns have the correct accept header"
                  (should= "application/vnd.hale+json"
                           (get (:headers (request (format "%s%s" "/v2/discoverable_resources/" "studies"))) "Content-Type"))))
 (it "supports /v2/discoverable_resources/ without name"
     (should-be successful? (request "/v2/discoverable_resources/"))))


(describe
 "unknown routes response"
 (it "return the correct error message"
     (should= "Not Found" (:body (request "random/path"))))
 (it "return the correct status code"
    (should= 404 (:status (request "random/path")))))

(describe
 "persisted entity"
 (it "returns an empty entity"
     (should= @(atom {:link-relation "" :href "" :resource-name ""}) @persisted-entity))
 (it "resets the enity"
     (do
       (reset! persisted-entity "something")
       (reset-persisted-entity!)
       (should= @(atom {:link-relation "" :href "" :resource-name ""}) @persisted-entity))))

(let [resource-name "studies"
      link-relation "http://localhost/alps/studies"
      href "http://localhost/studies"]
  (describe
   "finding one discoverable resources"
   (before (clean-database)
           (discoverable-resource-create
            resource-name link-relation href))
   (it "returns a map of the record"
       (should==
        {:resource_name resource-name
         :link_relation link-relation
         :href href}
        (discoverable-resource-first resource-name)))))

(let [resource-name "studies"
      link-relation "http://localhost/alps/studies"
      href "http://localhost/studies"]
  (describe
   "creates a discoverable resource"
   (before (clean-database))
   (it "creates one"
       (let [created (discoverable-resource-create
                      resource-name
                      link-relation
                      href)]
         (should== (conj
                    created
                    {:resource_name resource-name
                     :link_relation link-relation
                     :href href})
                   created))))
  (describe
   "duplications of discoverable resources"
   (before (clean-database)
           (discoverable-resource-create resource-name
                                         link-relation
                                         href))
   (it "returns an error"
       (should== {:errors
                  {:taken-by
                   {:resource_name resource-name
                    :link_relation link-relation
                    :href href}}}
                 (discoverable-resource-create resource-name
                                               link-relation
                                               href)))))


(describe
 "creating hrefs"
 (it "creates the correct map"
     (should==
      {:href "http://test.host/apath/of-stuff#shsnthsnth"}
      (link-href-build "apath/of-stuff#shsnthsnth"))))

(run-specs)
