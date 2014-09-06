(ns step-definitions.discoverable-resources-steps
  (:use wormhole-clj.core wormhole-clj.db cucumber.runtime.clj clojure.test)
  (:require [clojure.java.jdbc :as jdbc]
            [ring.adapter.jetty :as ring]
            [wormhole-clj.media :as media]
            [cheshire.core :refer :all :as json]
            [clj-http.client :as client]))

(defn table-to-map [table]
  (into {} (map vec (.raw table))))

(def test-port 57767)

(def last-response (atom nil))
(defn last-response-set! [new-response]
  (reset! last-response new-response))

(defn execute-get-request [path headers]
  (client/get (format "%s:%d/%s" "http://localhost" test-port path)
              {:headers headers}))

(defn verify-app-url [url]
  (when-let [result (re-matches
                   (re-pattern
                    (format "\\A%s(.*)" (java.util.regex.Pattern/quote (base-uri))))
                   url)]
    (let [response (execute-get-request
                    (let [path (nth result 1)]
                      (if (= "" path) "/" path)))]
      (is (= 200 (:status response))))))

(def server (atom nil))

(defn server-start []
  (if @server
    (throw (IllegalStateException. "Server already started."))
    (reset! server (ring/run-jetty handler
                                   {:port test-port
                                    :join? false}))))

(defn server-stop []
  (if (nil? @server)
    (throw (IllegalStateException. "Server already stopped."))
    (do
      (.stop @server)
      (reset! server nil))))

(Before [] (server-start))

(After []
       (reset! last-response nil)
       (server-stop)
       (jdbc/db-do-commands dbspec "TRUNCATE TABLE discoverable_resources;"))

(Given #"^a discoverable resource exists with the following attributes:$" [table]
       (let [table-map (table-to-map table)]
         (discoverable-resource-create
          (get table-map "resource_name")
          (get table-map "link_relation")
          (get table-map "href"))))

(When #"^I invoke the uniform interface method GET to \"([^\"]*)\" accepting \"([^\"]*)\"$" [path media-type]
      (last-response-set!
       (client/get (format "%s:%d/%s" "http://localhost" test-port path)
                   {:headers {"Accept" media-type}})))

(Then #"^I should get a status of (\d+)$" [status]
      (is (= (:status @last-response) (read-string status))))

(Then #"^the resource representation should have exactly the following properties:$" [table]
      (let [actual (into {} (remove (fn [[key item]] (= key media/keyword-links))
                                    (json/parse-string (:body @last-response) true)))
            map-of-table (table-to-map table)
            expected (zipmap
                      (map keyword (keys map-of-table))
                      (vals map-of-table))]
        (is (= expected actual))))

(Then #"^the resource representation should have exactly the following links:$" [table]
      (let [actual-links (get (json/parse-string (:body @last-response)) (name media/keyword-links))
            expected-links (into {} (rest (table-to-map table)))]
        (is (= (count expected-links)
               (count actual-links)))
        (is (= (keys expected-links)
               (keys actual-links)))
        (prn actual-links)
        (doall
         (map (fn [link]
                (let [[link-relation href] link]
                  (is (= (get-in actual-links [link-relation (name media/keyword-href)])))
                  (verify-app-url href)))
              expected-links))
        ))
