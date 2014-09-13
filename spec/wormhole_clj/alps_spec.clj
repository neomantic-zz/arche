(ns wormhole-clj.alps-spec
  (:use wormhole-clj.alps)
  (:require [speclj.core :refer :all]))


(describe
 "media types"
 (it "returns the correct value for json media type"
     (should= "application/alps+json" json-media-type)))

(describe
 "alps reserved key words"
 (it "returns the correct keyword for alps"
     (should= :alps keyword-alps))
 (it "returns the correct keyword for href"
     (should= :href keyword-href))
 (it "returns the correct keyword for type"
     (should= :type keyword-type))
 (it "returns the correct keyword for doc"
     (should= :doc keyword-doc))
 (it "returns the correct keyword for id"
     (should= :id keyword-id))
 (it "returns the correct keyword for value"
     (should= :value keyword-value))
 (it "returns the correct keyword for link"
     (should= :link keyword-link)))

(describe
 "alps reserved values"
 (it "returns correct string for semantic"
     (should= "semantic" type-value-semantic)))

(describe
 "alps schemas"
 (it "returns correct value for the URL schema"
     (should= "http://alps.io/schema.org/URL" schema-url))
 (it "returns correct value for the Text schema"
     (should= "http://alps.io/schema.org/Text" schema-text)))
