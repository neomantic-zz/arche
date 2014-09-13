(ns step-definitions.hypermedia-steps
  (:use step-definitions.discoverable-resources-steps
        cucumber.runtime.clj
        clojure.test)
  (:require [cheshire.core :refer :all :as json]))

(Then #"^I should receive the following \"([^\"]*)\" response:$" [media-type expected-response]
     (if (re-find #".+json\z" media-type)
       (is (= (json/parse-string expected-response true)
              (json/parse-string (:body @last-response) true)))
       (throw (Exception. "Currently only supports testing json responses."))))
