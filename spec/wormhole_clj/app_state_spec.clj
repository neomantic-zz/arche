(ns wormhole-clj.app-state-spec
  (:use wormhole-clj.app-state)
  (:require [speclj.core :refer :all]))

(describe
 "environment"
 (it "returns the correct base-uri"
     (should-not-be-nil (base-uri))))

(describe
 "expiry"
 (it "returns a cache expiry value"
     (should= 600 (cache-expiry))))

(describe
 "paths"
 (it "returns the correct string for the alps path"
     (should= "alps" alps-path)))

(describe
 "creating urls"
 (it "creates the correct url for an alps profile"
     (should= "http://test.host/v2/alps/FooBars"
              (alps-profile-url "FooBars")))
 (it "creates the correct url for an alps profile when resource name needs to be escaped"
     (should= "http://test.host/v2/alps/Foo%20Bars"
              (alps-profile-url "Foo Bars"))))