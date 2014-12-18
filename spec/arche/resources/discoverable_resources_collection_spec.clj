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
             :refer [required-descriptors
                     discoverable-resource-create
                     discoverable-resources-all
                     discoverable-resource-first] :as entity]
            [arche.core :refer [handler] :as web]))

(defn post-request [path accept-type content-type body]
  (web/handler (mock/header
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
  (web/handler (header
                (mock/request :get "/discoverable_resources")
                "Accept" mime-type)))

(defn create-paginateable [number-of]
  (doseq [i (range number-of)]
    (entity/discoverable-resource-create
     {:resource-name "studies"
      :link-relation-url "http://link-relation.io"
      :href "http://test.host/url/studies"})))

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
       (should= "application/hal+json,application/vnd.hale+json,application/json"
                (get-header response "Accept")))
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
        "max-age=600, private"
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
 "creating the hal map"
 (before (helper/clean-database))
 (after (helper/clean-database))
 (context
  "when there are none"
  (it "returns the correct map when there are none"
      (should==
       {:items []
        :_embedded {:items []}
        :_links {:self {:href (str "http://example.org/discoverable_resources?page=1&per_page=" default-per-page)}}}
       (hal-map (discoverable-resources-paginate))))
  (describe
   "creating links when there are no records"
   (it "does not include the prev link relation"
       (should-not-contain
        media/link-relation-prev
        (media/keyword-links (hal-map (discoverable-resources-paginate)))))
   (it "does not include the next link relation"
       (should-not-contain
        media/link-relation-next
        (media/keyword-links (hal-map (discoverable-resources-paginate)))))))
 (context
  "when there at least some"
  (it "returns the correct map when there is at least 1"
      (let [record (create-record)]
        (should== {:items [{:href (entity/url-for record)}]
                   :_embedded {:items [{
                                        :link_relation_url (:link_relation_url record)
                                        :href (:href record)
                                        :resource_name (:resource_name record)
                                        :_links {
                                                 :self {:href (entity/url-for record)}
                                                 }
                                        }
                                       ]}
                   :_links {:self {:href (str "http://example.org/discoverable_resources?page=1&per_page=" default-per-page)}}}
                  (hal-map (discoverable-resources-paginate))))))
 (describe
  "creating links when there are more than the per page"
  (before
   (create-paginateable (+ 1 default-per-page)))
  (it "includes the next link relation"
      (should= {:href "http://example.org/discoverable_resources?page=2&per_page=25"}
               (media/link-relation-next
                (media/keyword-links (hal-map (discoverable-resources-paginate)))))))
 (describe
  "creating links when there is a page"
  (before
   (create-paginateable (inc (* 2 default-per-page))))
  (it "includes the next link relation"
      (should= {:next {:href (str "http://example.org/discoverable_resources?page=3&per_page=" default-per-page)}
                :prev {:href (str "http://example.org/discoverable_resources?page=1&per_page=" default-per-page)}
                :self {:href (str "http://example.org/discoverable_resources?page=2&per_page=" default-per-page)}}
               (media/keyword-links (hal-map (discoverable-resources-paginate 2)))))))


(describe
 "all resources as hale representable collection"
 (before (helper/clean-database))
 (after (helper/clean-database))
 (context
  "when there are none"
  (it "returns the correct map when there are none"
      (should==
      {:items []
       :_embedded {:items []}
       :_links {:self {:href "http://example.org/discoverable_resources"}
                :create {:href "http://example.org/discoverable_resources"
                         :method "POST"
                         :data {:href {:type "text:text"}
                                :link_relation_url {:type "text:text"}
                                :resource_name {:type "text:text"}}}}}
      (hale-map {:page 0
                 :records []
                 :next-page false
                 :prev-page false
                 :per-page default-per-page}))))
 (context
  "when there at least some"
  (it "returns the correct map when there is at least 1"
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
                  :_links {:self {:href (str "http://example.org/discoverable_resources?page=1&per_page=" default-per-page)}
                           :create {:href "http://example.org/discoverable_resources"
                                    :method "POST"
                                    :data {:href {:type "text:text"}
                                           :link_relation_url {:type "text:text"}
                                           :resource_name {:type "text:text"}}}}}
                 (hale-map (discoverable-resources-paginate)))))))

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

(describe
 "getting paginated"
 (before-all (clean-database))
 (after-all (clean-database))
 (describe
  "getting all pages"
  (describe
   "when there are none"
   (with-all paginated (discoverable-resources-paginate))
   (it "returns false for previous"
       (should= false (:has-prev @paginated)))
   (it "returns false for next"
       (should= false (:has-next @paginated)))
   (it "returns no records"
       (should= 0 (count (:records @paginated)))))
  (describe
   "when there are more than none and less that the default"
   (before-all
    (create-paginateable 1))
   (with-all paginated (discoverable-resources-paginate))
   (it "returns a correct number of discoverables"
       (should= 1 (count (:records @paginated))))
   (it "returns 0 for next"
       (should= false (:has-next @paginated)))
   (it "returns false for prev"
       (should= false (:has-prev @paginated))))
  (describe
   "when there are more than the default"
   (before-all (create-paginateable (+ 1 default-per-page)))
   (with-all paginated (discoverable-resources-paginate))
   (it "returns a correct number of discoverables"
       (should= default-per-page (count (:records @paginated))))
   (it "returns true for next"
       (should= true (:has-next @paginated)))
   (it "returns false for prev"
       (should= false (:has-prev @paginated)))))
 (describe
  "getting a specific page"
  (describe
   "getting the first page"
   (describe
    "when there are none"
    (before (clean-database))
    (with-all paginated (discoverable-resources-paginate 1))
    (it "returns a correct number of discoverables"
        (should= 0 (count (:records @paginated))))
    (it "returns false for prev"
        (should= false (:has-prev @paginated)))
    (it "returns false for next"
        (should= false (:has-next @paginated))))
   (context
    "when there are less than the default"
    (before-all (create-paginateable 3))
    (with-all paginated (discoverable-resources-paginate 1))
    (it "returns false for next"
        (should= false (:has-next @paginated)))
    (it "returns false for prev"
        (should= false (:has-prev @paginated)))
    (it "returns only count of items below the max"
        (should= 3 (count (:records @paginated)))))
   (context
    "when there are none beyond the first page"
    (before-all
     (clean-database)
     (create-paginateable default-per-page))
    (with-all paginated (discoverable-resources-paginate 1))
    (it "returns only count of items below the max"
        (should= default-per-page (count (:records @paginated))))
    (it "returns false for prev"
        (should= false (:has-prev @paginated)))
    (it "returns false for next"
        (should= false (:has-next @paginated))))
   (describe
    "when there are more beyond the first page"
    (before-all (create-paginateable (+ 1 default-per-page)))
    (with-all paginated (discoverable-resources-paginate 1))
    (it "returns true for next"
        (should= true (:has-next @paginated)))
    (it "returns false for prev"
        (should= false (:has-prev @paginated)))
    (it "returns correct number of items"
        (should= default-per-page (count (:records @paginated))))))
  (describe
   "getting a page greater than the first"
   (describe
    "when no records exist"
    (before-all (clean-database))
    (with-all paginated (discoverable-resources-paginate 2))
    (it "returns false for prev"
        (should= false (:has-prev @paginated))))
   (describe
    "when that page has nothing more beyond it"
    (before-all
     (clean-database)
     (create-paginateable default-per-page))
    (with-all paginated (discoverable-resources-paginate 2))
    (it "returns the max number of available for that page"
        (should= 0 (count (:records @paginated))))
    (it "returns false for next"
        (should= false (:has-next @paginated)))
    (it "returns true for prev"
        (should= true (:has-prev @paginated))))
   (describe
    "when that page has 1 more beyond it"
    (before-all
     (clean-database)
     (create-paginateable (+ 1 default-per-page)))
    (with-all paginated (discoverable-resources-paginate 2))
    (it "returns the max number of available for that page"
        (should= 1 (count (:records @paginated))))
    (it "returns false for next"
        (should= false (:has-next @paginated)))
    (it "returns true for prev"
        (should= true (:has-prev @paginated))))
   (describe
    "when there are many pages beyond it"
    (before-all
     (create-paginateable (+ 1 (* default-per-page 2))))
    (with-all paginated (discoverable-resources-paginate 2))
    (it "returns the correct count of items - the maximum"
        (should= default-per-page (count (:records @paginated))))
    (it "returns true for next"
        (should= true (:has-next @paginated))))))
 (describe
  "getting a pages with per-page specified"
  (context
   "when getting first page with more than the max count"
   (describe
    "with maximum count"
    (before-all
     (create-paginateable (* 2 default-per-page)))
    (with-all paginated (discoverable-resources-paginate 1 80))
    (it "returns only the maximum"
       (should= default-per-page (count (:records @paginated))))
    (it "returns true for next"
        (should= true (:has-next @paginated)))
    (it "returns false for prev"
        (should= false (:has-prev @paginated))))
   (describe
    "when getting less than the max count"
    (before-all (create-paginateable default-per-page))
    (with-all paginated (discoverable-resources-paginate 1 24))
    (it "returns false for prev"
        (should= false (:has-prev @paginated)))
    (it "returns true for next"
        (should= true (:has-next @paginated)))
    (it "returns the correct count"
        (should= 24 (count (:records @paginated))))))
  (context
   "when getting a page greater than the first page"
   (describe
    "requesting more than the maximum count"
    (describe
     "and the amount of the next page equals the default"
     (before-all
      (clean-database)
      (create-paginateable (* 2 default-per-page)))
     (with-all paginated (discoverable-resources-paginate 2 30))
     (it "returns only the max"
         (should= default-per-page (count (:records @paginated))))
     (it "returns false for next"
         (should= false (:has-next @paginated)))
     (it "returns true for prev"
         (should= true (:has-prev @paginated))))
    (describe
     "and the next page has items before it"
     (before-all
      (create-paginateable (+ 1 (* 2 default-per-page))))
     (with-all paginated (discoverable-resources-paginate 2 30))
     (it "returns true for next"
         (should= true (:has-next @paginated)))))
   (describe
    "when getting less than the max count"
    (before-all
     (clean-database)
     (create-paginateable (* 2 default-per-page)))
    (with-all paginated (discoverable-resources-paginate 2 24))
    (it "returns true for prev"
        (should= true (:has-prev @paginated)))
    (it "returns true for next"
        (should= true (:has-next @paginated)))
    (it "returns the correct count"
        (should= 24 (count (:records @paginated))))))))

(describe
 "creating links"
 (it "returns a self link"
     (should= {media/keyword-href (str "http://example.org/discoverable_resources?page=1&per_page=" default-per-page)}
               (media/link-relation-self
                (hal-links {:page 1
                            :per-page default-per-page
                            :has-next false
                            :has-prev false}))))
(it "returns a self link with pagination query pags"
     (should= {media/keyword-href (str "http://example.org/discoverable_resources?page=2&per_page=" default-per-page)}
               (media/link-relation-self
                (hal-links {:has-next true
                            :has-prev true
                            :per-page default-per-page
                            :next-page 3
                            :prev-page 1
                            :page 2}))))
(it "returns a next link"
     (should= {media/keyword-href (str "http://example.org/discoverable_resources?page=2&per_page=" default-per-page)}
               (media/link-relation-next
                (hal-links {:page 1
                            :has-next true
                            :next-page 2
                            :per-page default-per-page}))))
 (it "returns both a prev and a next link"
     (should== {media/link-relation-self {media/keyword-href (str "http://example.org/discoverable_resources?page=2&per_page=" default-per-page)}
                media/link-relation-prev {media/keyword-href (str "http://example.org/discoverable_resources?page=1&per_page=" default-per-page)}
                media/link-relation-next {media/keyword-href (str "http://example.org/discoverable_resources?page=3&per_page=" default-per-page)}}
               (hal-links {:prev-page 1
                           :has-prev true
                           :has-next true
                           :next-page 3
                           :page 2
                           :per-page default-per-page})))
 (it "returns a prev link"
     (should= {media/keyword-href (str "http://example.org/discoverable_resources?page=1&per_page=" default-per-page)}
               (media/link-relation-prev
                (hal-links {:prev-page 1
                            :has-prev true
                            :page 2
                            :per-page default-per-page})))))

(describe
 "converting pagination query params"
 (it "returns page 1 when no page is passed"
     (should== [1 1]
               (query-params->pagination-params {"per_page" "1"})))
 (it "returns page 1 when page is 0"
     (should== [1 1]
               (query-params->pagination-params {"page" "0", "per_page" "1"})))
 (it "returns page 1 when page is below zero"
     (should== [1 1]
               (query-params->pagination-params {"page" "-1", "per_page" "1"})))
 (it "returns page number when it is above 1"
     (should== [24 1]
               (query-params->pagination-params {"page" "24", "per_page" "1"})))
 (it "returns page 1 when page is below set to some other string"
     (should== [1 1]
               (query-params->pagination-params {"page" "snthsnhnth", "per_page" "1"})))
 (it "returns the default per page when it's not provided"
     (should== [1 default-per-page]
               (query-params->pagination-params {"page" "1"})))
 (it "returns 0 per page when 0 is requested"
     (should== [1 0]
               (query-params->pagination-params {"page" "1", "per_page" "0"})))
 (it "returns 0 per page when -1 is requested"
     (should== [1 0]
               (query-params->pagination-params {"page" "1", "per_page" "-1"})))
 (it "returns 0 per page when something random is requested"
     (should== [1 0]
               (query-params->pagination-params {"page" "1", "per_page" "shsnhsh"})))
 (it "returns the per page passed when a value greater than the default is provided"
     (should== [1 default-per-page]
               (query-params->pagination-params {"page" "1", "per_page" "80"})))
 (it "returns the per page passed when it not provided"
     (should== [1 3]
               (query-params->pagination-params {"page" "1", "per_page" "3"}))))
