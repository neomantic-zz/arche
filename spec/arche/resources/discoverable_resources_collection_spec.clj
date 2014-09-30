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
            [ring.util.response :only [:get-header] :as ring]
            [arche.media :refer :all :as media]
            [arche.resources.discoverable-resource
             :refer [required-descriptors] :as entity]
            [arche.core :refer [app] :as web]))

(defn post-request [path accept-type content-type body]
  (web/app (mock/header
            (mock/header (mock/request :post path body)
                         "Accept" accept-type)
            "Content-Type" content-type)))

(defmacro as-json [args]
  `(cheshire.core/generate-string
    ~args))

(describe
 "creating a resource"
 (before (helper/clean-database))
 (after (helper/clean-database))
 (it "returns 201"
     (should= 201 (:status (post-request
                            "/discoverable_resources"
                            "application/hal+json"
                            "application/json"
                            (as-json
                             {:link_relation "a"
                              :href "a"
                              :resource_name "a"})))))
 (it "handles valid json"
     (should= 201 (:status (post-request
                            "/discoverable_resources"
                            "application/hal+json"
                            "application/json"
                            (as-json
                             {:link_relation "http://test.host/alps/users"
                              :href "http://test.host/users"
                              :resource_name "users"})))))
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
               (:body (post-request
                       "/discoverable_resources"
                       "application/hal+json"
                       "application/json"
                       (j/generate-string
                        {:link_relation "http://test.host/alps/users"
                         :href "http://test.host/users"
                         :resource_name "users"}))) true)))
)

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
       (should= "application/hal+json" (ring/get-header response "Accept")))))

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
                                    :resource_name "users"}))))))
 )
