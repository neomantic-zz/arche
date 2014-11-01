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
            [arche.app-state :as app]
            [arche.http :refer [etag-make]]
            [arche.validations :refer [errors-key]]
            [arche.resources.discoverable-resource
             :refer [required-descriptors discoverable-resource-first] :as entity]
            [arche.core :refer [app] :as web]))

(defn post-request [path accept-type content-type body]
  (web/app (mock/header
            (mock/header (mock/request :post path body)
                         "Accept" accept-type)
            "Content-Type" content-type)))
(defn create-record []
  (entity/discoverable-resource-create
   {:resource-name "studies"
    :link-relation-url "http://example.org/alps/studies"
    :href "http://example.org/studies"}))

(defn to-json [args]
  (cheshire.core/generate-string
    args))

(defn from-json [args]
  (cheshire.core/parse-string
    args true))

(defn valid-post []
  (post-request
   "/discoverable_resources"
   "application/hal+json"
   "application/json"
   (to-json
    {:link_relation_url "https://test.host/alps/users"
     :href "https://test.host/users"
     :resource_name "users"})))

(def mime-hale media/hale-media-type)
(def mime-hal media/hal-media-type)

;; FIXME can't get with "/" on the end
(defn make-get-request [mime-type]
  (app (header
        (mock/request :get "/discoverable_resources")
        "Accept" mime-type)))

(describe
 "creating a resource"
 (before (helper/clean-database))
 (after (helper/clean-database))
 (it "returns 201"
     (should= 201 (:status (valid-post))))
 (it "handles valid json"
     (should= 201 (:status (valid-post))))
 (it "response with the correct body"
     (should= {:link_relation_url "https://test.host/alps/users"
               :href "https://test.host/users"
               :resource_name "users"
               media/keyword-links
               {media/link-relation-self {media/keyword-href
                                          (entity/url-for {:resource_name "users"})}
                media/link-relation-profile {media/keyword-href
                                             entity/profile-url}}}
              (from-json
               (:body (valid-post))))))

(let [resource-name "users"
      response (post-request
                     "/discoverable_resources"
                     "application/hal+json"
                     "application/json"
                     (to-json
                      {:link_relation_url "https://test.host/alps/users"
                       :href "https://test.host/users"
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
       (should-not-be-nil (get-header response "ETag")))))

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
       (should= "application/vnd.hale+json,application/hal+json" (get-header response "Accept")))))

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
  "empty"
  (it "returns the correct message"
      (should==
               {:errors
                {:href ["can't be blank" "is not valid"]
                 :link_relation_url ["can't be blank" "is not valid"]
                 :resource_name ["can't be blank"]}}
               (from-json
                (:body
                 (post-request "/discoverable_resources"
                               "application/hal+json"
                               "application/json"
                               "")))))
  (it "returns the correct status"
      (should= 422 (:status
                    (post-request "/discoverable_resources"
                                  "application/hal+json"
                                  "application/json"
                                  "")))))
(describe
 "testing processablity"
 (it "returns the correct map if href is absent"
     (should== {errors-key {:href [:blank :invalid]
                           :resource_name [:blank]
                           :link_relation_url [:blank :invalid]}
                :href ""}
               (test-processable {:href ""}))))
 (describe
  "unprocessable - missing attribute date"
  (it "returns the correct status code when json does not any of the require keys"
      (should= 422 (:status
                    (post-request "/discoverable_resources"
                                  "application/hal+json"
                                  "application/json"
                                  "{\"a\": 1}"))))
  (it "returns the correct status code when json does include link_relation_url"
      (should= 422 (:status
                    (post-request "/discoverable_resources"
                                  "application/hal+json"
                                  "application/json"
                                  (to-json
                                   {:href "http://service.io/users"
                                    :resource_name "users"})))))
  (it "returns the correct status code when json does include resource name property"
      (should= 422 (:status
                    (post-request "/discoverable_resources"
                                  "application/hal+json"
                                  "application/json"
                                  (to-json
                                   {:link_relation_url "http://service.io/alps/Users"
                                    :href "http://service.io/users"})))))
  (it "returns the correct status code when json does include href"
      (should= 422 (:status
                    (post-request "/discoverable_resources"
                                  "application/hal+json"
                                  "application/json"
                                  (to-json
                                   {:link_relation_url "http://service.io/alps/Users"
                                    :resource_name "users"}))))))
 (it "returns the correct message when resource_name is missing"
     (should== {:errors {:resource_name ["can't be blank"]}}
              (from-json (:body (post-request "/discoverable_resources"
                                              "application/hal+json"
                                              "application/json"
                                              (to-json
                                               {:href "https://service.io/users"
                                                :link_relation_url "https://service.io/alps/Users"}))))))
 (it "returns the correct message when href is missing"
     (should== {:errors {:href ["can't be blank" "is not valid"]}}
               (from-json (:body (post-request "/discoverable_resources"
                                               "application/hal+json"
                                               "application/json"
                                               (to-json
                                                {:resource_name "users"
                                                 :link_relation_url "https://service.io/alps/Users"})))))))

(describe
   "creating duplicates"
   (before (clean-database)
           (entity/discoverable-resource-create
            {:resource-name "studies"
             :link-relation-url "http://example.org/alps/studies"
             :href "http://example.org/studies"}))
   (after (clean-database))
   (it "returns 400 trying to create an existing resource"
       (let [response
             (post-request "/discoverable_resources"
                           "application/hal+json"
                           "application/json"
                           (to-json
                            {:resource_name "studies"
                             :link_relation_url "http://example.org/alps/studies"
                             :href "http://example.org/studies"}))]
         (should= 400 (:status response))))
   (it "returns the corret body trying to create an existing resource"
       (let [response
             (post-request "/discoverable_resources"
                           "application/hal+json"
                           "application/json"
                           (to-json
                            {:resource_name "studies"
                             :link_relation_url "http://example.org/alps/studies"
                             :href "http://example.org/studies"}))]
         (should= {:errors {:resource_name ["is already taken"]}}
                  (from-json (:body response)))))
   (it "returns the correct content-type"
       (let [response
             (post-request "/discoverable_resources"
                           "application/hal+json"
                           "application/json"
                           (to-json
                            {:resource_name "studies"
                             :link_relation_url "http://example.org/alps/studies"
                             :href "http://example.org/studies"}))]
         (should= "application/json"
                  (get-header response "Content-Type")))))


(describe
 "headers"
 (it "returns the correct cache-control header"
     (should= "max-age=0, private"
              (get-header
               (post-request "/discoverable_resources"
                             "application/hal+json"
                             "application/json"
                             (to-json
                              {:href "https://service.io/users"
                               :link_relation_url "https://service.io/alps/Users"}))
               "Cache-Control")))
 (it "returns the correct content-type"
     (should= "application/json"
              (get-header
               (post-request "/discoverable_resources"
                             "application/hal+json"
                             "application/json"
                             (to-json
                              {:href "https://service.io/users"
                               :link_relation_url "https://service.io/alps/Users"}))
               "Content-Type"))))

(doseq [mime-type [mime-hale mime-hal]]
  (describe
   (format "getting all using the %s mime-type" mime-type)
   (before (helper/clean-database))
   (after (helper/clean-database))
   (it "is successful when there are none"
       (should= 200 (:status (make-get-request mime-type))))
   (it "is successful when there are some"
       (do
         (create-record)
         (should= 200 (:status (make-get-request mime-type)))))
   (it "is parsable json"
       (should-not-throw (from-json (:body (make-get-request mime-type)))))
   (it "returns a Cache-control header"
       (should=
        "max-age=0, private"
        (get-header (make-get-request mime-type) "Cache-control")))
   (it "returns a Content-type header"
       (should=
        (if (= mime-type mime-hale) mime-hale
            mime-hal)
        (get-header (make-get-request mime-type) "Content-Type")))
   (it "returns a Etag header"
       (should-not-be-nil
        (get-header (make-get-request mime-type) "ETag")))
   (it "returns the correct accept header"
       (should=
        "application/vnd.hale+json,application/hal+json"
        (get-header (make-get-request mime-type) "Accept")))
   (it "returns the correct location header"
       (should=
        "http://example.org/discoverable_resources"
        (get-header (make-get-request mime-type) "Location")))))

(describe
 "all resources as representable collection"
 (before (helper/clean-database))
 (after (helper/clean-database))
 (context
  "when there are none"
  (it "returns the correct map when there are none"
      (should==
      {:items []
       :_embedded {:items []}
       :_links {:self {:href "http://example.org/discoverable_resources"}}}
      (hypermedia-map []))))
 (context
  "when there at least some"
  (it "returns the corret map when there is at least 1"
     (let [record (create-record)]
       (should== {:items [
                          {:href (entity/url-for record)}
                          ]
                  :_embedded {:items [{
                                       :link_relation_url (:link_relation_url record)
                                       :href (:href record)
                                       :resource_name (:resource_name record)
                                       :_links {
                                                :self {:href (entity/url-for record)}
                                                }
                                       }
                                      ]}
                  :_links {:self {:href "http://example.org/discoverable_resources"}}}
                 (hypermedia-map [record]))))))

(describe
 "processable"
 (it "returns errors when everything is missing"
	 (should= {errors-key {:href [:blank :invalid]
                          :link_relation_url [:blank :invalid]
                          :resource_name [:blank]}}
			  (test-processable {})))
 (it "returns errors when everything is empty"
     (should= {errors-key
               {:href [:blank :invalid]
                :link_relation_url [:blank :invalid]
                :resource_name [:blank]}
               :href ""
               :link_relation_url ""
               :resource_name ""}
    		  (test-processable {:href ""
                                 :link_relation_url ""
                                 :resource_name ""})))
 (it "should have no errors"
     (should== {:href "https://a-path"
                :link_relation_url "https://another-path"
                :resource_name "some-name"}
    		   (test-processable {:href "https://a-path"
                                  :link_relation_url "https://another-path"
                                  :resource_name "some-name"})))

 (it "returns invalid, and not blank when href is not url"
     (should-contain :invalid
                     (get-in (test-processable {:href "hsthsnthsnthtnh"})
                             [errors-key :href]))
     (should-not-contain :blank
    					 (get-in (test-processable {:href "hsthsnthsnthtnh"})
                                 [errors-key :href])))
 (it "returns invalid, and not blank when link relation is not url"
     (should-contain :invalid
                     (get-in
                      (test-processable
                       {:link_relation_url "hsthsnthsnthtnh"})
                      [errors-key :link_relation_url]))
     (should-not-contain :blank
                         (get-in
                          (test-processable
                           {:link_relation_url "hsthsnthsnthtnh"})
                          [errors-key :link_relation_url])))
 (it "does not return invalid when href is https"
     (should-not-contain :invalid
                         (get-in (test-processable
                                  {:href "http://service.io/hello"})
                                 [errors-key :href])))
 (it "does not return invalid when href is https"
     (should-not-contain :invalid
                         (get-in (test-processable
                                  {:href "http://service.io/hello"})
                                 [errors-key :href])))
 (it "does not return invalid when link relation is https"
     (should-not-contain :invalid
                         (get-in
                          (test-processable
                           {:link_relation_url "https://service.io/hello"})
                          [errors-key :link_relation_url])))
 (it "does not return invalid when link relation is http"
     (should-not-contain :invalid
                         (get-in
                          (test-processable
                           {:link_relation_url "https://service.io/hello"})
                          [errors-key :link_relation_url]))))
