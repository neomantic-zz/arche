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
     (should= (format "%s/discoverable_resources/studies" (app/base-uri))
              (discoverable-resource-entity-url "studies")))
 (it "creates the correct discoverable resource entity url with url encoding"
     (should= (format "%s%s" (app/base-uri) "/discoverable_resources/bad%20resource%20name")
              (discoverable-resource-entity-url "bad resource name"))))

(describe
 "etag"
 (it "creates one"
     (should-not-be-nil (etag-make "discoverable_resources" {:id 3
                                                             :updated_at 3})))
 (it "should be a string"
     (should-be string? (etag-make "discoverable_resources" {:id 3
                                                             :updated_at 3}))))

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
   (before (clean-database)
           (discoverable-resource-create
            resource-name link-relation href))
   (it "returns a map of the record"
       (should==
        {:resource_name resource-name
         :link_relation link-relation
         :href href}
        (discoverable-resource-first resource-name)))))

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
