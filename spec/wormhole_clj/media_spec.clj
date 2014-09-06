(ns wormhole-clj.media-spec
  (:require [speclj.core :refer :all]
            [wormhole-clj.media :refer :all]))

(describe
 "keywords"
 (it "returns the correct keyword for links"
     (should= :_links keyword-links))
 (it "returns the correct keyword for href"
     (should= :href keyword-href))
 (it "returns the correct keyword for self"
     (should= :self link-relation-self)))

(describe
 "media types"
 (it "returns the correct string for hale+json"
     (should= "application/vnd.hale+json" hale-media-type)))
