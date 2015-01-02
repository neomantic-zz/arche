;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;;  arche - A hypermedia resource discovery service
;;
;;  https://github.com/neomantic/arche
;;
;;  Copyright:
;;    2015
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

(ns arche.initialize-spec
  (:require [arche.initialize :refer :all]
            [arche.test-support :refer :all]
            [clojure.java.jdbc :refer [db-do-commands]]
            [arche.resources.discoverable-resource :refer [discoverable-resource-first
                                                           discoverable-resource-create
                                                           names]]
            [arche.config :refer :all]
            [speclj.core :refer :all]))

(describe
 "seeds the entry point"
 (context
  "when its not registered"
  (before (truncate-database))
  (after (truncate-database))
  (it "creates the record"
      (let [{:keys [resource_name link_relation_url href]} (seed-entry-point)]
        (should= (str base-uri "/alps/DiscoverableResources") link_relation_url)
        (should= "discoverable_resources" resource_name)
        (should= (str base-uri "/discoverable_resources") href))))
 (context
  "when its already registered"
  (before (truncate-database))
  (after (truncate-database))
  (with existing (seed-entry-point))
  (it "returns the existing one"
      (should== @existing (seed-entry-point))))
 (context
  "when it already exists"
  (before (truncate-database)
          (discoverable-resource-create {:resource-name "discoverable_resources"
                                         :href "http://not-the-same/foobars"
                                         :link-relation-url "http://not-the-same/alps/Foos"}))
  (after (truncate-database))
  (it "returns a new one"
      (let [{:keys [resource_name link_relation_url href]} (seed-entry-point)]
        (should= (str base-uri "/alps/DiscoverableResources") link_relation_url)
        (should= "discoverable_resources" resource_name)
        (should= (str base-uri "/discoverable_resources") href)))
  (it "deletes the old one"
      (do
        (seed-entry-point)
        (let [{:keys [resource_name link_relation_url href]} (discoverable-resource-first "discoverable_resources")]
          (should-not= "http://not-the-same/alps/Foos" link_relation_url)
          (should-not= "http://not-the-same/foobars" href))))))
