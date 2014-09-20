(ns wormhole-clj.entry-points-spec
  (:use wormhole-clj.resources.entry-points)
  (:require [speclj.core :refer :all]
            [wormhole-clj.core-spec :only (factory-discoverable-resource-create
                                           clean-database) :as support]
            [wormhole-clj.resources.discoverable-resources
             :only (discoverable-resource-first)
             :as record]
            [clojurewerkz.urly.core :as urly]
            [wormhole-clj.media :as media]
            [wormhole-clj.app-state :as app]))

(def expected-profile-url
  (.toString
   (.mutatePath
    (urly/url-like (app/base-uri))
    "/alps/EntryPoints")))

(def expected-type-url
  (format "%s#%s"
          (.mutatePath
           (urly/url-like (app/base-uri))
           "/alps/EntryPoints")
          "entry_points"))

(def expected-self-url
  (.toString (.mutatePath
              (urly/url-like (app/base-uri))
              route)))

(describe
 "route"
 (it "returns the correct route"
     (should= "/" route)))

(describe
 "names"
 (it "returns the correct string for titleized"
     (should= "EntryPoints" (:titleized names)))
 (it "returns the correct value for the alps type"
     (should= "entry_points" (:alps-type names))))

(let [subject (entry-points-map)
      get-href-value (fn [link-relation]
                       (get-in subject [media/keyword-links link-relation media/keyword-href]))]
  (describe
   "its map for serialization"
   (it "it includes links"
       (should-contain media/keyword-links subject))
   (it "contains a profile link in it's collection of links"
       (should-contain :profile (media/keyword-links subject)))
   (it "contains the correct value for the profiles href"
       (should= expected-profile-url
                (get-href-value media/link-relation-profile)))
   (it "contains a profile link in it's collection of links"
       (should-contain :type (media/keyword-links subject)))
   (it "contains the correct value for the type href"
       (should= expected-type-url
                (get-href-value  media/link-relation-type)))
   (it "contains a self link in it's collection of links"
       (should-contain :self (media/keyword-links subject)))
   (it "contains the correct value for the self href"
       (should= expected-self-url
                (get-href-value media/link-relation-self)))))

(describe
 "urls"
 (it "returns the correct profile url"
     (should= expected-profile-url (profile-url)))
 (it "returns the correct type url"
     (should= expected-type-url (type-url))))

(let [default-link-relations #{media/link-relation-self
                                media/link-relation-type
                                media/link-relation-profile}
      link-relations (fn [map]
                       (media/keyword-links map))
      test-resource-name "studies"]
  (context
   "with nothing to discover"
   (before
    (support/clean-database))
   (describe
    "empty entry-points map"
    (it "has correct default links and nothing more"
        (should== default-link-relations
         (into #{} (keys (link-relations (entry-points-map))))))))
  (context
   "with something to discover"
   (describe
    "entry-points map"
    (before
     (support/factory-discoverable-resource-create test-resource-name))
    (it "has the default links"
        (pending "figure out reduce"))
    (it "has more items than the default links"
        (should (>
                 (count (link-relations (entry-points-map)))
                 (count default-link-relations))))
    (it "includes a keyword for the discoverable resource"
        (should-contain (keyword test-resource-name)
                        (link-relations (entry-points-map))))
    (it "includes the correct href value for the discoverable resource"
        (let [resource (record/discoverable-resource-first test-resource-name)]
          (if resource
            (should= (:href resource)
                     (media/keyword-href (get (link-relations (entry-points-map)) (keyword test-resource-name))))
            (should-not-be-nil resource)))))))
