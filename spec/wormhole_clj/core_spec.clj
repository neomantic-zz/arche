(ns wormhole-clj.core-spec
  (:use wormhole-clj.db)
  (:require [speclj.core :refer :all]
            [clojure.java.jdbc :as jdbc]
            [wormhole-clj.core :refer :all]
            [wormhole-clj.resources.profiles :refer :all :as profile]
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
                           (get (:headers (request (format "%s%s" "/v2/discoverable_resources/" "studies"))) "Content-Type")))))

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
 "creating urls"
 (it "creates the correct discoverable resource entity url"
     (should= "http://test.host/v2/discoverable_resources/studies"
              (discoverable-resource-entity-url "studies")))
 (it "creates the correct discoverable resource entity url with url encoding"
     (should= "http://test.host/v2/discoverable_resources/bad%20resource%20name"
              (discoverable-resource-entity-url "bad resource name"))))


(describe
 "etag"
 (it "creates one"
     (should-not-be-nil (entity-etag-make "discoverable_resources" {:id 3
                                                             :updated_at 3})))
 (it "should be a string"
     (should-be string? (entity-etag-make "discoverable_resources" {:id 3
                                                             :updated_at 3}))))

(describe
 "alps"
 (it "creates the correct representation of an alps document"
     (should== {:alps
                {:descriptor
                 [{:href "http://alps.io/schema.org/URL"
                   :type "semantic"
                   :id "link_relation"
                   :doc {:value "The LinkRelation of the DiscoverableResource"}}
                  {:href "http://alps.io/schema.org/URL"
                   :type "semantic"
                   :id "href"
                   :doc {:value "The HREF to the entry point of the DiscoverableResource"}}
                  {:href "http://alps.io/schema.org/Text"
                   :type "semantic"
                   :id "resource_name"
                   :doc {:value "The name of the DiscoverableResource"}}
                  {:rt "discoverable_resource"
                   :type "safe"
                   :id   "show"
                   :doc {:value "Returns an individual DiscoverableResource"}}
                  {:descriptor
                   [{:href "link_relation"}
                    {:href "href"}
                    {:href "resource_name"}
                    {:href "show"}]
                   :type "semantic"
                   :id "discoverable_resource"
                   :link
                   [{:href "http://test.host/v2/alps/DiscoverableResources#discoverable_resource"
                     :rel "self"}]
                   :doc
                   {:value "A Resource that can be discovered via an entry point"}}]
                 :link [{:href "http://test.host/v2/alps/DiscoverableResources"
                         :type "self"}]
                 :doc {:value "Describes the semantics, states and state transitions associated with DiscoverableResources."}}}
               (discoverable-resource-alps-representation))))

(describe
 "registered profiles"
 (it "has registered discovered resources profile"
     (should-not-be-nil (registered-profile-get :discoverable-resources))))

(run-specs)
