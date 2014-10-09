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

(ns step-definitions.discoverable-resources-steps
  (:use arche.resources.discoverable-resource
        cucumber.runtime.clj
        clojure.test)
  (:require [arche.media :as media]
            [step-definitions.test-helpers :refer :all]
            [cheshire.core :refer :all :as json]))

(Before []
        (server-start)
        (database-truncate))

(After []
       (reset! last-response nil)
       (server-stop)
       (database-truncate))

(Given #"^a discoverable resource exists with the following attributes:$" [table]
       (let [table-map (table-to-map table)]
         (discoverable-resource-create
          (get table-map "resource_name")
          (get table-map "link_relation")
          (get table-map "href"))))

(When #"^I invoke the uniform interface method GET to \"([^\"]*)\" accepting \"([^\"]*)\"$" [path media-type]
      (last-response-set!
       (execute-get-request path {"Accept" media-type})))


(When #"^I invoke uniform interface method POST to \"([^\"]*)\" with the \"([^\"]*)\" body and accepting \"([^\"]*)\" responses:$" [path content-type accept-type body]
      (let [headers {"Accept" accept-type
                     "Content-Type" content-type}]
        (last-response-set!
         (execute-post-request
          path
          headers
          (try
            (json/generate-string
             (json/parse-string body))
            (catch Exception e
              (prn "That wasn't json")))))))

(Then #"^I should get a status of (\d+)$" [status]
      (is (= (last-response-status) (read-string status))))

(Then #"^the resource representation should have exactly the following properties:$" [table]
      (let [actual (into {} (remove (fn [[key item]] (= key media/keyword-links))
                                    (json/parse-string (last-response-body) true)))
            map-of-table (table-to-map table)
            expected (zipmap
                      (map keyword (keys map-of-table))
                      (vals map-of-table))]
        (is (= (count expected) (count actual)))
        (is (= (into #{} (keys expected)) (into #{} (keys actual))))
        (doall
         (map (fn [pair]
                (let [[key value] pair]
                  (is (= value (key actual)))))
              expected))))

(Then #"^I should get a response with the following errors:$" [table]
      (let [response-map (json/parse-string (last-response-body) true)]
        (is (not (nil? (get response-map :errors))))
        (doall
         (map (fn [[attribute message]]
                (is (some #(= % message) (get-in response-map [:errors (keyword attribute)]))
                    (format "expected '%s' in attribute '%s'; got; '%s'"
                            message
                            attribute
                            (clojure.string/join ", " (get-in response-map [:errors (keyword attribute)])) )))
              (rest (map vec (.raw table)))))))
