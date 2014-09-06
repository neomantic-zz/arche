(ns wormhole-clj.http-spec
  (:require [speclj.core :refer :all]
            [wormhole-clj.http :refer :all]))


(describe
 "building headers"
 (it "returns a map with the correct key for the location header"
     (should= {"Location" "a-url"}
              (header-location "a-url")))
 (it "returns a map with the correct key for the accept header"
     (should= {"Accept" "a-content-type"}
              (header-accept "a-content-type"))))
