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
        arche.resources.profiles)
  (:require [speclj.core :refer :all]
            [cheshire.core :refer :all :as json]
            [arche.db :refer [cache-key] :as record]
            [arche.http :refer [etag-make] :as http-helper]
            [arche.app-state :refer :all :as app]))

(let [resource-name "studies"
      link-relation "http://example.org/alps/studies"
      href "http://example.org/studies"]
  (describe
   "creates a discoverable resource"
   (before (clean-database))
   (it "creates one"
       (let [created (discoverable-resource-create
                      resource-name
                      link-relation
                      href)]
         (should== {:resource_name resource-name
                    :link_relation  link-relation
                    :id (:id created)
                    :created_at (:created_at created)
                    :updated_at (:updated_at created)
                    :href href}
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
     (should= (format "%s/discoverable_resources/studies" (app/base-uri))
              (discoverable-resource-entity-url "studies")))
 (it "creates the correct discoverable resource entity url with url encoding"
     (should= (format "%s%s" (app/base-uri) "/discoverable_resources/bad%20resource%20name")
              (discoverable-resource-entity-url "bad resource name"))))

(describe
 "alps"
 (it "returns the correct profile url"
     (should= profile-url (format "%s/alps/%s"(app/base-uri) "DiscoverableResources")))
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
      link-relation "http://example.org/alps/studies"
      href "http://example.org/studies"]
  (describe
   "finding one discoverable resources"
   (before (clean-database))
   (after (clean-database))
   (it "returns a map of the record"
       (let [created (discoverable-resource-create
                      resource-name link-relation href)
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

(describe
 "getting all discoverables"
 (before
  (clean-database)
  (discoverable-resource-create "studies" "http://link-relation.io" "http://test.host/url/studies"))
 (it "returns a correct number of discoverables"
     (should= 1 (count (discoverable-resources-all)))))


(describe
 "correct representable map"
 (before (clean-database))
 (after (clean-database))
 (it "produces the correct map to serialize"
     (let [name "studies"
           href   "http://test.host/url/studies"
           link-relation  "http://link-relation.io"
           mappable (discoverable-resource-create name link-relation href)]
       (should==
        {:link_relation link-relation
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
           link-relation  "http://link-relation.io"
           mappable {:link_relation link-relation
                     :href href
                     :resource_name name
                     :created_at "sometime"
                     :updated_at "sometime"}]
       (should==
        {:link_relation link-relation
         :href href
         :resource_name name
         :_links {
                  :self {:href "http://example.org/discoverable_resources/studies"}
                  :profile {:href "http://example.org/alps/DiscoverableResources"}
                  }}
        (hypermedia-map mappable)))))

(describe
 "properties"
 (it "returns the required descriptor for response and requests"
     (should= [:resource_name :link_relation :href] required-descriptors)))


(let [resource-name "studies"]
  (describe
   "entity etags"
   (before (clean-database))
   (after (clean-database))
   (it "returns the same etag value for both created and found entities"
       (let [created (discoverable-resource-create
                      resource-name "http://example.org/alps/studies" "http://example.org/studies")
             found (discoverable-resource-first resource-name)]
         (should= (record/cache-key "discoverable_resources" created) (record/cache-key "discoverable_resources" found))))))


(let [resource-name "studies"
      record (discoverable-resource-create
              resource-name "http://example.org/alps/studies" "http://example.org/studies")
      test-response (:response (ring-response-json record 200))]
  (describe
  "response as json"
  (it "returns the correct headers"
      (should== {"ETag" (http-helper/etag-make (record/cache-key (:routable names) record))
                 "Cache-Control" "max-age=600, private"
                 "Accept" "application/hal+json"
                 "Location" (discoverable-resource-entity-url resource-name)}
                (:headers test-response)))
  (it "returns parsable json"
      (should-not-throw (json/parse-string (:body test-response))))
  (it "returns the correct status code"
      (should= 200 (:status test-response)))))

(describe
 "validations"
 (it "returns false when url is valid"
     (should= false (url-valid? "g")))
 (it "returns false when url is valid"
     (should= false (url-valid? "http://g")))
 (it "returns true on valid url"
     (should= true (url-valid? "https://shsnhsnh.io/snthnth#thth?query=2")))
 (it "returns correct error key when url is not valid"
     (should== [:invalid] (validate-url "http://what")))
 (it "returns empty map when resource name present"
     (should== {} (validate-resource-name {:resource_name "studies"})))
 (it "returns the vector with :blank when resource name is empty"
     (should== {:resource_name [:blank]} (validate-resource-name {:resource_name ""})))
 (it "returns the vector with :blank when resource name is missing"
     (should== {:resource_name [:blank]} (validate-resource-name {})))
 (it "returns vector with :blank and :format when href empty"
     (should== {:href [:blank :invalid]} (validate-href {:href ""})))
 (it "returns vector with :blank and :format when href missing"
     (should== {:href [:blank :invalid]} (validate-href {})))
 (it "returns vector with :invalid when invalid href"
     (should== {:href [:invalid]} (validate-href {:href "http://what"})))
 (it "returns empty map when href valid"
     (should== {} (validate-href {:href "https://what"})))
 (it "returns vector with :format when invalid link-relation"
     (should== {:link_relation [:invalid]} (validate-link-relation {:link_relation "http://what"})))
 (it "returns vector with :blank and :format when link-relation empty"
     (should== {:link_relation [:blank :invalid]} (validate-link-relation {:link_relation ""})))
 (it "returns vector with :format when link-relation is missing"
     (should== {:link_relation [:blank :invalid]} (validate-link-relation {})))
 (it "returns empty map when link relation valid"
     (should== {} (validate-link-relation {:link_relation "https://what"})))
 (it "validates"
     (should= {:href [:blank :invalid]
               :link_relation [:blank :invalid]
               :resource_name [:blank]}
               (validate {:href "" :link_relation "" :resource_name ""}))))
