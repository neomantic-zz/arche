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
              ;; get the rows, and make the link relation keywords
              rows ((fn [rows]
                      (zipmap (map keyword (keys rows))
                              (vals rows))) (rest expected))
              ;; add the headers to the rows
              rows-with-headers (map (fn [row]
                                      {(first headers) (first row)
                                       (last headers) (last row)})
                                     rows)]
          (doseq [expected-link rows-with-headers]
            (let [{:keys [rel href]} expected-link
                  existing (filter (fn [{rel :rel}]
                                     (= (keyword rel) (:rel expected-link)))
                                   received-links)]
              ;; not the best, when one of these fails, clojure.test keeps testing
              (is (not (empty? existing))
                   (format "expect link with link relation '%s'; got %s " (name (:rel expected-link)) received-links))
              (is (= 1 (count existing))
                  (format "found more that one link with link relation '%s'; got %s" (name (:rel expected-link)) received-links))
              (is (= (:href expected-link) (:href (first existing)))))))
        (throw (Exception. (format "The response was missing a link %s" (last-response-body))))))

(Then #"^the \"([^\"]*)\" document should have a \"([^\"]*)\" descriptor with the following properties:$" [media-type descriptor-id table]
      (throw-when-not-json! media-type)
      (if-let [descriptors (get-in (json/parse-string (last-response-body) true)
                                   [a/keyword-alps
                                    a/keyword-descriptor])]
        (let [expected (table-rows-map table)
              expected-keys (map keyword (keys expected))
              properties (zipmap expected-keys
                                 (vals expected))
              existing (filter #(= (a/keyword-id %) descriptor-id) descriptors)]
          (if (nil? (first existing))
              (throw (Exception. (format "A descriptor with id '%s' was not found; got %s" descriptor-id descriptors)))
              (if (> (count existing) 1)
                (throw (Exception. (format "More than one descriptor with id '%s' was found; got %s" descriptor-id descriptors)))
                (let [found (first existing)]
                  (doseq [property-key expected-keys]
                    (is (not (nil? (property-key found)))
                        (format "The descriptor with id '%s' was missing the property '%s'; got %s"
                                descriptor-id
                                property-key
                                found))
                    ;; doc key is special, the documentation lives in a :value key
                    (if (= property-key a/keyword-doc)
                      (is (= (property-key properties)
                             (get-in found [property-key a/keyword-value]))
                          (format "The value of the property '%s' was not equal; got %s" (name property-key) found))
                      (is (= (property-key properties)
                             (property-key found))
                          (format "The value of the property '%s' was not equal; got %s" (name property-key) found))))))))
        (throw (Exception. (format "The response was missing descriptors: %s" (last-response-body))))))

(Then #"^the \"([^\"]*)\" descriptor should have the following descriptors:$" [descriptor-id table]
      (if-let [descriptors (get-in (json/parse-string (last-response-body) true)
                                   [a/keyword-alps
                                    a/keyword-descriptor])]
        (let [existing (filter #(= (a/keyword-id %) descriptor-id) descriptors)]
          (if (nil? (first existing))
            (throw (Exception. (format "A descriptor with id '%s' was not found; got %s" descriptor-id descriptors)))
            (if (> (count existing) 1)
              (throw (Exception. (format "More than one descriptor with id '%s' was found; got %s" descriptor-id descriptors)))
              (let [found (map #(a/keyword-href %) (a/keyword-descriptor (first existing)))
                    relative-links (map #(first %) (rest (.raw table)))]
                (doseq [link relative-links]
                  (is (some #(= % link) found)
                      (format "Unable to find '%s' in set of descriptors: got %s" link (first existing))))))))
        (throw (Exception. (format "The response was missing descriptors: %s" (last-response-body))))))

(Then #"^the \"([^\"]*)\" descriptor should have the following link:$" [descriptor-id table]
      (if-let [descriptors (get-in (json/parse-string (last-response-body) true)
                                   [a/keyword-alps
                                    a/keyword-descriptor])]
        (let [existing (filter #(= (a/keyword-id %) descriptor-id) descriptors)]
          (if (nil? (first existing))
            (throw (Exception. (format "A descriptor with id '%s' was not found; got %s" descriptor-id descriptors)))
            (if (> (count existing) 1)
              (throw (Exception. (format "More than one descriptor with id '%s' was found; got %s" descriptor-id descriptors)))
              (let [found (first existing)]
                (if-let [link (a/keyword-link found)]
                  (let [expected-properties (first (rest (.raw table)))
                        rel (first expected-properties)
                        href (last expected-properties)]
                    (is (= {a/keyword-href href a/keyword-rel rel}
                           link)))
                  (throw (Exception. (format "The descriptor was missing a link; got %s" found))))))))
        (throw (Exception. (format "The response was missing descriptors: %s" (last-response-body))))))
