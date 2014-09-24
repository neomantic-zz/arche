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

(ns step-definitions.hypermedia-steps
  (:use step-definitions.discoverable-resources-steps
        cucumber.runtime.clj
        clojure.test)
  (:require [cheshire.core :only (parse-string) :as json]
            [arche.media :as media]
            [environ.core :refer [env]])
  (:import [java.net URI URL]))

(def hosted-link-relations
  #{media/link-relation-profile
    media/link-relation-self
    media/link-relation-type})

(Then #"^I should receive the following \"([^\"]*)\" response:$" [media-type expected-response]
      (if (re-find #".+json\z" media-type)
        (is (= (json/parse-string expected-response true)
               (json/parse-string (:body @last-response) true)))
        (throw (Exception. "Currently only supports testing json responses."))))

(Then #"^the resource representation should have exactly the following links:$" [table]
      (let [actual-links (get (json/parse-string (last-response-body)) (name media/keyword-links))
            expected-links (table-rows-map table)]
        ;; make sure the same number of links are present
        (is (= (count expected-links) (count actual-links))
            (format "expected links for %s; got %s" expected-links actual-links))
        ;; make sure the same links relations are there
        (is (= (into #{} (keys expected-links)) (into #{} (keys actual-links)))
            "received incorrect link relations")
        ;; check the hrefs
        (doseq  [expected-link expected-links]
          (let [[link-relation href] expected-link]
            (is (= (URI. (link-href-get link-relation actual-links)) (URI. href)))
            (when (contains? hosted-link-relations link-relation)
              (verify-app-url href media/hal-media-type))))))
