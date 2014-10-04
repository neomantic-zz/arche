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

(ns arche.resources.discoverable-resources-collection-spec
  (:use arche.resources.discoverable-resources-collection)
  (:require [speclj.core :refer :all]
            [arche.core-spec :refer [clean-database] :as helper]
            [cheshire.core :refer [generate-string parse-string] :as j]
            [ring.mock.request :refer [header request] :as mock]
            [ring.util.response :refer [get-header]]
            [arche.media :refer :all :as media]
            [arche.db :refer [cache-key]]
            [arche.http :refer [etag-make]]
            [arche.resources.discoverable-resource
             :refer [required-descriptors discoverable-resource-first] :as entity]
            [arche.core :refer [app] :as web]))

(defn post-request [path accept-type content-type body]
  (web/app (mock/header
            (mock/header (mock/request :post path body)
                         "Accept" accept-type)
            "Content-Type" content-type)))

(defmacro as-json [args]
  `(cheshire.core/generate-string
    ~args))

(defn valid-post []
  (post-request
   "/discoverable_resources"
   "application/hal+json"
   "application/json"
   (as-json
    {:link_relation "http://test.host/alps/users"
     :href "http://test.host/users"
     :resource_name "users"})))

(describe
 "creating a resource"
 (before (helper/clean-database))
 (after (helper/clean-database))
 (it "returns 201"
     (should= 201 (:status (valid-post))))
 (it "handles valid json"
     (should= 201 (:status (valid-post))))
 (it "response with the correct body"
     (should= {:link_relation "http://test.host/alps/users"
               :href "http://test.host/users"
               :resource_name "users"
               media/keyword-links
               {media/link-relation-self {media/keyword-href
                                          (entity/discoverable-resource-entity-url "users")}
                media/link-relation-profile {media/keyword-href
                                             entity/profile-url}}}
              (j/parse-string
               (:body (valid-post)) true))))

(let [resource-name "users"
      response (post-request
                     "/discoverable_resources"
                     "application/hal+json"
                     "application/json"
                     (as-json
                      {:link_relation "http://test.host/alps/users"
                       :href "http://test.host/users"
                       :resource_name resource-name}))
      record (entity/discoverable-resource-first resource-name)]
  (describe
   "headers on valid post"
   (before (helper/clean-database))
   (after (helper/clean-database))
   (it "returns correct location header"
       (should= "http://example.org/discoverable_resources/users" (get-header response "Location")))
   (it "returns correct accept header"
       (should= "application/hal+json" (get-header response "Accept")))
   (it "returns correct cache control header"
       (should= "max-age=600, private" (get-header response "Cache-Control")))
   (it "returns correct etag"
       (should= (etag-make (cache-key "discoverable_resources" record))
                (get-header response "Etag")))))

(let [response (post-request
                "/discoverable_resources"
                "application/xml"
                "application/json"
                "{}")]
  (describe
   "error requests"
   (it "returns 406 when the accept type is not hal+json"
       (should= 406 (:status response)))
   (it "returns the accept in the header"
       (should= "application/hal+json" (get-header response "Accept")))))

(let [response (post-request "/discoverable_resources"
                             "application/hal+json"
                             "application/xml"
                             "{}")]
  (describe
   "rejecting content-types"
   (it "returns the correct status"
       (should= 415 (:status response)))
   (it "returns the correct message"
       (should= "Unsupported media type. Currently only supports application/json"
                (:body response)))))

(context
 "handling bad body"
 (describe
  "invalid json"
  (it "returns the correct status code when the json cannot be parsed"
      (should= 400 (:status
                    (post-request "/discoverable_resources"
                                  "application/hal+json"
                                  "application/json"
                                  "{\"a\"}"))))
  (it "returns the correct message when the json cannot be parsed"
      (should= "Required valid content for Content-Type applicaton/json"
               (:body
                (post-request "/discoverable_resources"
                              "application/hal+json"
                              "application/json"
                              "{\"a\"}")))))
 (describe
  "unprocessable - missing attribute date"
  (it "returns the correct status code when json does not any of the require keys"
      (should= 422 (:status
                    (post-request "/discoverable_resources"
                                  "application/hal+json"
                                  "application/json"
                                  "{\"a\": 1}"))))
  (it "returns the correct status code when json does include link-relation"
      (should= 422 (:status
                    (post-request "/discoverable_resources"
                                  "application/hal+json"
                                  "application/json"
                                  (j/generate-string
                                   {:href "http://service.io/users"
                                    :resource_name "users"})))))
(it "returns the correct status code when json does include resource name property"
      (should= 422 (:status
                    (post-request "/discoverable_resources"
                                  "application/hal+json"
                                  "application/json"
                                  (j/generate-string
                                   {:link_relation "http://service.io/alps/Users"
                                    :href "http://service.io/users"})))))
  (it "returns the correct status code when json does include href"
      (should= 422 (:status
                    (post-request "/discoverable_resources"
                                  "application/hal+json"
                                  "application/json"
                                  (j/generate-string
                                   {:link_relation "http://service.io/alps/Users"
                                    :resource_name "users"})))))))
