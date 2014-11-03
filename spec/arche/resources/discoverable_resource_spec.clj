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

(ns arche.resources.discoverable-resource-spec
  (:use arche.core-spec
        arche.resources.discoverable-resource
        arche.resources.profiles
        arche.core)
  (:require [speclj.core :refer :all]
            [cheshire.core :refer :all :as json]
            [arche.db :refer [cache-key] :as record]
            [ring.mock.request :refer :all :as ring-mock]
            [ring.util.response :only [:get-header] :as ring]
            [ring.util.codec :only [:url-encode]]
            [arche.http :refer [etag-make] :as http-helper]
            [arche.media :refer :all :as media]
            [arche.app-state :refer :all :as app]))

(defn mock-request [resource_name mime-type]
  (header
   (ring-mock/request :get (format "/discoverable_resources/%s" (ring.util.codec/url-encode resource_name)))
   "Accept" mime-type))

(defn make-request [mock-request]
  (app mock-request))

(let [resource-name "studies"]
  (doseq [mime-type [media/hale-media-type media/hal-media-type media/json-media-type]]
    (describe
     (format "GET discoverable_resources/{resource_name} accepting %s" mime-type)
     (before
      (clean-database)
      (factory-discoverable-resource-create resource-name))
     (after (clean-database))
     (describe
      "headers"
      (it "should have the correct accept header"
          (should= "application/hal+json,application/vnd.hale+json,application/json"
                   (-> (mock-request resource-name mime-type)
                       make-request
                       (ring/get-header "Accept"))))
      (it "returns the correct location header"
          (should= (format "%s/discoverable_resources/studies" (app/base-uri))
                   (-> (mock-request resource-name mime-type)
                       make-request
                       (ring/get-header "Location"))))
      (it "returns an etag when resource exists"
             (should-not-be-nil
              (-> (mock-request resource-name mime-type)
                  make-request
                  (ring/get-header "Etag"))))
      (it "does not return an etag when resource does not exists"
             ;; test added, because liberator always hits the etag
             ;; method
             (should-be-nil
              (-> (mock-request "nobodies" mime-type)
                  make-request
                  (ring/get-header "Etag"))))))))


(let [resource-name "studies"
      link-relation-url "http://example.org/alps/studies"
      href "http://example.org/studies"]
  (describe
   "creates a discoverable resource"
   (before (clean-database))
   (it "creates one"
       (let [created (discoverable-resource-create
                      {:resource-name resource-name
                       :link-relation-url link-relation-url
                       :href href})]
         (should== {:resource_name resource-name
                    :link_relation_url  link-relation-url
                    :id (:id created)
                    :created_at (:created_at created)
                    :updated_at (:updated_at created)
                    :href href}
                   created)))))

(describe
 "creating urls"
 (it "creates the correct discoverable resource entity url"
     (should= (format "%s/discoverable_resources/studies" (app/base-uri))
              (url-for {:resource_name "studies"})))
 (it "creates the correct discoverable resource entity url with url encoding"
     (should= (format "%s%s" (app/base-uri) "/discoverable_resources/ugly%20resource%20name")
              (url-for {:resource_name "ugly resource name"}))))

(describe
 "alps"
 (it "returns the correct profile url"
     (should= profile-url (format "%s/alps/%s"(app/base-uri) "DiscoverableResources")))
 (it "creates the correct representation of an alps document"
     (should== {:alps
                {:descriptor
                 [{:href "http://alps.io/schema.org/URL"
                   :type "semantic"
                   :id "link_relation_url"
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
                   [{:href "link_relation_url"}
                    {:href "href"}
                    {:href "resource_name"}
                    {:href "show"}]
                   :type "semantic"
                   :id "discoverable_resource"
                   :link
                   {:href "http://example.org/alps/DiscoverableResources#discoverable_resource"
                    :rel "self"}
                   :doc
                   {:value "A Resource that can be discovered via an entry point"}}]
                 :link {:href "http://example.org/alps/DiscoverableResources"
                        :rel "self"}
                 :doc {:value "Describes the semantics, states and state transitions associated with DiscoverableResources."}}}
               (discoverable-resource-alps-representation))))

(describe
 "registered profiles"
 (it "has registered discovered resources profile"
     (should-not-be-nil (registered-profile-get :discoverable-resources))))

(let [resource-name "studies"
      link-relation-url "http://example.org/alps/studies"
      href "http://example.org/studies"]
  (describe
   "finding one discoverable resources"
   (before (clean-database))
   (after (clean-database))
   (it "returns a map of the record"
       (let [created (discoverable-resource-create
                      {:resource-name resource-name
                       :link-relation-url link-relation-url
                       :href href})
             found (discoverable-resource-first resource-name)]
         (should== created found)))))

(describe
 "name management"
 (it "returns titlized name"
     (should= "DiscoverableResources" (:titleized names)))
 (it "returns routable name"
     (should= "discoverable_resources" (:routable names)))
 (it "returns tabelized name"
     (should= :discoverable_resources (:tableized names)))
 (it "returns singular name"
     (should= "discoverable_resource" (:singular names)))
 (it "returns keywordified name"
     (should= :discoverable-resources (:keyword names))))

(defn create-paginateable [number-of]
  (doseq [i (range number-of)]
    (discoverable-resource-create
     {:resource-name "studies"
      :link-relation-url "http://link-relation.io"
      :href "http://test.host/url/studies"})))



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
       (should= 0 (:prev-page @paginated))
       (should= false (:has-prev @paginated)))
   (it "returns false for next"
       (should= 0 (:next-page @paginated))
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
   (it "returns false for next"
       (should= 0 (:next-page @paginated))
       (should= false (:has-next @paginated)))
   (it "returns false for prev"
       (should= 0 (:prev-page @paginated))
       (should= false (:has-prev @paginated))))
  (describe
   "when there are more than the default"
   (before-all (create-paginateable (+ 1 default-per-page)))
   (with-all paginated (discoverable-resources-paginate))
   (it "returns a correct number of discoverables"
       (should= default-per-page (count (:records @paginated))))
   (it "returns true for next"
       (should= 2 (:next-page @paginated))
       (should= true (:has-next @paginated)))
   (it "returns false for prev"
       (should= 0 (:prev-page @paginated))
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
        (should= 0 (:prev-page @paginated))
        (should= false (:has-prev @paginated)))
    (it "returns false for next"
        (should= 0 (:next-page @paginated))
        (should= false (:has-next @paginated))))
   (context
    "when there are less than the default"
    (before-all (create-paginateable 3))
    (with-all paginated (discoverable-resources-paginate 1))
    (it "returns false for next"
        (should= 0 (:next-page @paginated))
        (should= false (:has-next @paginated)))
    (it "returns false for prev"
        (should= 0 (:prev-page @paginated))
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
        (should= 0 (:prev-page @paginated))
        (should= false (:has-prev @paginated)))
    (it "returns false for next"
        (should= 0 (:next-page @paginated))
        (should= false (:has-next @paginated))))
   (describe
    "when there are more beyond the first page"
    (before-all (create-paginateable (+ 1 default-per-page)))
    (with-all paginated (discoverable-resources-paginate 1))
    (it "returns true for next"
        (should= 2 (:next-page @paginated))
        (should= true (:has-next @paginated)))
    (it "returns false for prev"
        (should= 0 (:prev-page @paginated))
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
    ;; FIXME - this returns false, when it should be true
    ;; my algorithm only peeks ahead, not behind
    ;; (it "returns true for prev"
    ;;     (should= true (:has-prev @paginated)))
    )
   (describe
    "when that page has nothing more beyond it"
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
    "when there are more pages beyond it"
    (before-all
     (create-paginateable (+ 1 (* default-per-page 2))))
    (with-all paginated (discoverable-resources-paginate 2))
    (it "returns the correct count of items - the maximum"
        (should= default-per-page (count (:records (discoverable-resources-paginate 2)))))
    (it "returns true for next"
       (should= true (:has-next (discoverable-resources-paginate 2)))))))
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
        (should= 2 (:next-page @paginated))
        (should= true (:has-next @paginated)))
    (it "returns false for prev"
        (should= 0 (:prev-page @paginated))
        (should= false (:has-prev @paginated))))
   (describe
    "when getting less than the max count"
    (before-all (create-paginateable default-per-page))
    (with-all paginated (discoverable-resources-paginate 1 24))
    (it "returns false for prev"
        (should= 0 (:prev-page @paginated))
        (should= false (:has-prev @paginated)))
    (it "returns true for next"
        (should= 2 (:next-page @paginated))
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
     (before-all (create-paginateable (+ 1 (* 2 default-per-page))))
     (with-all paginated (discoverable-resources-paginate 2 30))
     (it "returns true for next"
         (should= 3 (:next-page @paginated))
         (should= true (:has-next @paginated)))))
   (describe
    "when getting less than the max count"
    (before-all
     (create-paginateable (* 2 default-per-page)))
    (with-all paginated (discoverable-resources-paginate 2 24))
    (it "returns true for prev"
        (should= 1 (:prev-page @paginated))
        (should= true (:has-prev @paginated)))
    (it "returns true for next, when there are more"
        (should= 3 (:next-page @paginated))
        (should= true (:has-next @paginated)))
    (it "returns the correct count"
        (should= 24 (count (:records @paginated))))))))


(describe
 "getting all discoverables"
 (before (clean-database))
 (after (clean-database))
 (it "returns a correct number of discoverables"
     (discoverable-resource-create
      {:resource-name "studies"
       :link-relation-url "http://link-relation.io"
       :href "http://test.host/url/studies"})
     (should= 1 (count (discoverable-resources-all)))))

(describe
 "correct representable map"
 (before (clean-database))
 (after (clean-database))
 (it "produces the correct map to serialize"
     (let [name "studies"
           href   "http://test.host/url/studies"
           link-relation-url  "http://link-relation.io"
           mappable (discoverable-resource-create
                     {:resource-name name
                       :link-relation-url link-relation-url
                       :href href})]
       (should==
        {:link_relation_url link-relation-url
         :href href
         :resource_name name
         :_links {
                  :self {:href "http://example.org/discoverable_resources/studies"}
                  :profile {:href "http://example.org/alps/DiscoverableResources"}
                  }}
        (hypermedia-map mappable))))
 (it "removes timesstamp info"
     (let [name "studies"
           href   "http://test.host/url/studies"
           link-relation-url  "http://link-relation.io"
           mappable {:link_relation_url link-relation-url
                     :href href
                     :resource_name name
                     :created_at "sometime"
                     :updated_at "sometime"}]
       (should==
        {:link_relation_url link-relation-url
         :href href
         :resource_name name
         :_links {:self {:href "http://example.org/discoverable_resources/studies"}
                  :profile {:href "http://example.org/alps/DiscoverableResources"}}}
        (hypermedia-map mappable)))))

(describe
 "properties"
 (it "returns the required descriptor for response and requests"
     (should= [:resource_name :link_relation_url :href] required-descriptors)))


(let [resource-name "studies"]
  (describe
   "entity etags"
   (before (clean-database))
   (after (clean-database))
   (it "returns the same etag value for both created and found entities"
       (let [created (discoverable-resource-create
                      {:resource-name resource-name
                       :link-relation-url "http://example.org/alps/studies"
                       :href "http://example.org/studies"})
             found (discoverable-resource-first resource-name)]
         (should= (record/cache-key "discoverable_resources" created) (record/cache-key "discoverable_resources" found))))))


(let [resource-name "studies"
      record
      (discoverable-resource-create
       {:resource-name resource-name
        :link-relation-url "http://example.org/alps/studies"
        :href "http://example.org/studies"})
      test-response (:response (ring-response-json record 200))]
  (describe
   "response as json"
   (before (clean-database))
   (after (clean-database))
   (it "returns parsable json"
       (should-not-throw (json/parse-string (:body test-response))))
   (it "returns the correct status code"
       (should= 200 (:status test-response)))))

(describe
 "validating"
 (describe
  "urls"
  (it "returns false when url is valid"
      (should= false (url-valid? "g")))
  (it "returns true when url is valid"
      (should= true (url-valid? "http://g")))
  (it "returns true on valid url"
      (should= true (url-valid? "https://shsnhsnh.io/snthnth#thth?query=2")))
  (it "returns correct error key when url is not valid"
      (should== [:invalid] (validate-url "sthnshusnthh"))))
 (describe
  "uniqueness"
  (it "returns empty map with a resource with the same name does not exist"
      (should== {:resource_name "studies"}
                (validate-uniqueness
                    {:resource_name "studies"})))
  (it "returns empty map when no resource name was submitted"
      (should== {} (validate-uniqueness
                   {})))
  (it "returns empty map when an empty resource name was submitted"
      (should== {:resource_name ""}
                (validate-uniqueness
                   {:resource_name ""})))
  (it "returns the correct key when a resource already has the same resource_name"
      (let [resource-name "studies"]
        (discoverable-resource-create
         {:resource-name resource-name
          :link-relation-url "http://example.org/alps/studies"
          :href "http://example.org/studies"})
        (should== {:resource_name "studies"
                   :_*errors {:resource_name [:taken]}}
                  (validate-uniqueness
                   {:resource_name "studies"}))
        (clean-database))))
 (describe
  "link_relation_url"
  (it "returns empty map when link_relation_url is valid with http"
      (should== {}
                (validate-link-relation
                 {:link_relation_url "http://what.io"})))
  (it "returns empty map when linkt_relation_url is valid with https"
      (should== {}
                (validate-link-relation
                 {:link_relation_url "https://what.io"})))
  (it "returns correct key when not a uri"
      (should== {:link_relation_url [:invalid]}
                (validate-link-relation
                 {:link_relation_url "x1sh/daithff="})))
  (it "returns correct key when link_relation_url is not https/http"
      (should== {:link_relation_url [:invalid]}
                (validate-link-relation
                 {:link_relation_url "mailto:calbers@neomantic"})))
  (it "returns an correct when no link_relation_url is submitted"
      (should== {:link_relation_url [:blank :invalid]}
                (validate-link-relation
                 {})))
  (it "returns an correct when an empty link_relation_url is submitted"
      (should== {:link_relation_url [:blank :invalid]}
                (validate-link-relation
                 {:href ""}))))
 (describe
  "href"
  (it "returns empty map when href is valid with http"
      (should== {}
                (validate-href
                 {:href "http://what.io"})))
  (it "returns empty map when href is valid with https"
      (should== {}
                (validate-href
                 {:href "https://what.io"})))
  (it "returns correct key when not a uri"
      (should== {:href [:invalid]}
                (validate-href
                 {:href "x1sh/daithff="})))
  (it "returns correct key when href is not https/http"
      (should== {:href [:invalid]}
                (validate-href
                 {:href "mailto:calbers@neomantic"})))
  (it "returns an correct when no href is submitted"
      (should== {:href [:blank :invalid]}
                (validate-href
                 {})))
  (it "returns an correct when an empty href is submitted"
      (should== {:href [:blank :invalid]}
                (validate-href
                 {:href ""}))))
 (describe
  "presence of resource_name"
  (it "returns an empty map when the name is submitted"
      (should== {}
                (validate-resource-name-present
                 {:resource_name "studies"})))
  (it "returns a map with the correct key when no resource name was submitted"
      (should== {:resource_name [:blank]}
                (validate-resource-name-present
                 {})))
  (it "returns a map with the correct key where an empty resource name was submitted"
      (should== {:resource_name [:blank]}
                (validate-resource-name-present
                 {:resource_name ""})))))

(describe
 "etags"
 (before (clean-database))
 (after (clean-database))
 (it "can generates an etag"
     (let [record
           (discoverable-resource-create
            {:resource-name "studies"
             :link-relation-url "http://example.org/alps/studies"
             :href "http://example.org/studies"})]
       (should-not-be-nil (etag-for record)))))
