(ns step-definitions.discoverable-resources-steps
  (:use wormhole-clj.core
        wormhole-clj.db
        cucumber.runtime.clj
        clojure.test)
  (:refer-clojure :exclude [resolve])
  (:require [clojure.java.jdbc :as jdbc]
            [ring.adapter.jetty :as ring]
            [wormhole-clj.media :as media]
            [clojurewerkz.urly.core :as urly]
            [wormhole-clj.app-state :as app]
            [cheshire.core :refer :all :as json]
            [clj-http.client :as client])
  (:import [java.net URI URL]))

;; cucumber helpers
(defn table-to-map [table]
  (into {} (map vec (.raw table))))

(defn table-rows-map [table]
  (into {} (rest (table-to-map table))))

(def test-port 57767)

(defn url-to-test [path]
  (let [url (urly/url-like "http://localhost")
        with-port (.mutatePort url test-port)]
    (.toString (.mutatePath with-port path))))

(def last-response (atom nil))

(defn last-response-set! [new-response]
  (reset! last-response new-response))

(defn last-response-property [property]
  (fn [] (get @last-response property)))

(def last-response-body (last-response-property :body))
(def last-response-headers (last-response-property :headers))
(def last-response-status (last-response-property :status))

(defn link-href-get [link-relation-type links]
  (get-in links [link-relation-type (name media/keyword-href)]))

(defn execute-get-request [path headers]
  (client/get (url-to-test path) {:headers headers}))

(defn verify-app-url [url]
  (when (= (urly/host-of url) (urly/host-of (app/base-uri)))
    (let [path (urly/path-of (urly/url-like url))
          response (execute-get-request path {"Accept" "application/vnd.hale+json"})]
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
       (execute-get-request path {"Accept" media-type})))

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

(Then #"^the resource representation should have exactly the following links:$" [table]
      (let [actual-links (get (json/parse-string (last-response-body)) (name media/keyword-links))
            expected-links (table-rows-map table)]
        ;; make sure the same number of links are present
        (is (= (count expected-links) (count actual-links)))
        ;; make sure the same links relations are there
        (is (= (into #{} (keys expected-links)) (into #{} (keys actual-links))))
        ;; check the hrefs
        (doall
         (map (fn [link]
                (let [[link-relation href] link]
                  (is (= (link-href-get link-relation actual-links) href))
                  (verify-app-url href)))
              expected-links))))
