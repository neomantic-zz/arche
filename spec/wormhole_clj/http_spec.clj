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
              (header-accept "a-content-type")))
 (it "returns a map with the correct key for a cache control header"
     (should= {"Cache-Control" "some value"}
             (header-cache-control "some value")))
 (it "returns a private cache controller header map with max age"
     (should= {"Cache-Control" "max-age=600, private"}
              (cache-control-header-private-age 600))))
