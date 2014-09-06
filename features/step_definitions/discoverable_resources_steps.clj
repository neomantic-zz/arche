(ns step-definitions.discoverable-resources-steps
  (:use wormhole-clj.core wormhole-clj.db cucumber.runtime.clj clojure.test)
  (:require [clojure.java.jdbc :as jdbc]
            [ring.adapter.jetty :as ring]
            [cheshire.core :refer :all :as json]
            [clj-http.client :as client]))

(defn table-to-map [table]
  (into {} (map vec (.raw table))))

(defn table-to-list [table]
  (into () (map vec (.raw table))))

(def test-port 57767)

(def last-response (atom nil))
(defn last-response-set! [new-response]
  (reset! last-response new-response))


;; borrowed from here.
;;https://github.com/ring-clojure/ring/blob/master/ring-jetty-adapter/test/ring/adapter/test/jetty.clj#L25
(defmacro with-server [app options & body]
  `(let [server# (ring/run-jetty ~app ~(assoc options :join? false))]
     (try
       ~@body
       (finally (.stop server#)))))

(After []
       (reset! last-response nil)
       (jdbc/db-do-commands dbspec "TRUNCATE TABLE discoverable_resources;"))

(Given #"^a discoverable resource exists with the following attributes:$" [table]
       (let [table-map (table-to-map table)]
         (discoverable-resource-create
          (get table-map "resource_name")
          (get table-map "link_relation")
          (get table-map "href"))))

(When #"^I invoke the uniform interface method GET to \"([^\"]*)\" accepting \"([^\"]*)\"$" [path media-type]
      (with-server handler {:port test-port}
        (last-response-set!
         (client/get (format "%s:%d/%s" "http://localhost" test-port path)
                     {:headers {"Accept" media-type}}))))

(Then #"^I should get a status of (\d+)$" [status]
      (is (= (:status @last-response) (read-string status))))

(Then #"^the resource representation should have exactly the following properties:$" [table]
      (let [actual (into {} (remove (fn [[key item]] (= key :_links))
                                    (json/parse-string (:body @last-response) true)))
            map-of-table (table-to-map table)
            expected (zipmap
                      (map keyword (keys map-of-table))
                      (vals map-of-table))]
        (is (= expected actual))))

(Then #"^the resource representation should have exactly the following links:$" [table]
      (let [actual-links (get (json/parse-string (:body @last-response)) "_links")
            expected-links (into {} (rest (table-to-map table)))]
        (is (= (count expected-links)
               (count actual-links)))
        (is (= (keys expected-links)
               (keys actual-links)))
        (doall
         (map (fn [link]
                (let [[link-relation href] link]
                  (= (get actual-links link-relation)
                     href)))
              expected-links))
        ))
