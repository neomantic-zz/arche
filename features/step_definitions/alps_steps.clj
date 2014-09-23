(ns step-definitions.alps-steps
  (:use cucumber.runtime.clj
        step-definitions.discoverable-resources-steps
        clojure.test)
  (:require [cheshire.core :only (parse-string) :as json]
            [wormhole-clj.media :refer :all :as m]
            [wormhole-clj.alps :only [keyword-link
                                      keyword-alps
                                      keyword-rel
                                      keyword-href] :as a]))

(defn throw-when-not-json! [media-type]
  (when (not (= media-type  a/json-media-type))
    (throw (Exception. (format "Only %s documents are currently testable" a/json-media-type)))))

(Given #"^I follow the link to the \"([^\"]*)\" accepting \"([^\"]*)\"$" [link-relation-type media-type]
       (if-let [links (m/keyword-links  (json/parse-string (last-response-body) true))]
         (if-let [link ((keyword link-relation-type) links)]
           (let [{status :status, :as response} (call-app-url (a/keyword-href link) media-type)]
             (if (= 200 status)
               (last-response-set! response)
               (throw (Exception. (unexpected-response-message
                                   (a/keyword-href link)
                                   response)))))
           (throw (Exception. (format "The document is missing a link with a %s link relation %s"
                                      link-relation-type (last-response-body)))))
         (throw (Exception. (format "The application/hale+json document doesn't have link %s" (last-response-body))))))

(Then #"^I should receive an \"([^\"]*)\" with the following attributes:$" [media-type table]
      (throw-when-not-json! media-type)
      (let [body (json/parse-string (last-response-body))]
        (doseq [row (table-rows-map table)]
          (is (= (get body (first row) (last row)))
              (format "expected attribute pair %s, but not found in %s" row (last-response-body))))))

(Then #"^the \"([^\"]*)\" document should have the following links:$" [media-type table]
      (throw-when-not-json! media-type)
      (if-let [received-links (get-in (json/parse-string (last-response-body) true)
                                      [a/keyword-alps
                                       a/keyword-link])]
        (let [expected (table-to-map table)
              headers (map keyword (first expected))
              rows ((fn [i]
                      (zipmap (map keyword (keys i))
                              (vals i)))
                    (into {} (rest expected)))
              rows-with-headers (map (fn [row]
                                      {(first headers)
                                       (first row)
                                       (last headers)
                                       (last row)})
                                    rows)]
          (doseq [expected-link rows-with-headers]
            (let [exists (filter #(contains? % (:rel expected-link))
                                 received-links)]
              (prn exists)
              (is (not (nil? exists))))))
        (throw (Exception. (format "The response was missing a link %s" (last-response-body))))))

(Then #"^the \"([^\"]*)\" document should have the following descriptor:$" [media-type table]
      (throw-when-not-json! media-type)
      (if-let [descriptors (get-in (json/parse-string (last-response-body) true)
                                   [a/keyword-alps
                                    a/keyword-descriptor])]
        (let [expected (table-rows-map table)
              expected-keys (map keyword (keys expected))
              properties (zipmap expected-keys
                                 (vals expected))]
          (doseq [property-key expected-keys]
            (is (true? (contains? descriptors) property-key))
            (is (= (property-key properties)
                   (property-key descriptors)))))
        (throw (Exception. (format "The response was missing descriptors %s" (last-response-body))))))

(Then #"^the descriptor with id \"([^\"]*)\" should have the following descriptors:$" [arg1 arg2]
  (comment  Express the Regexp above with the code you wish you had  )
  (throw (cucumber.runtime.PendingException.)))


(Then #"^the descriptor with id should have the following link:$" [arg1]
  (comment  Express the Regexp above with the code you wish you had  )
  (throw (cucumber.runtime.PendingException.)))
