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
  (:use arche.core
        arche.db
        arche.resources.discoverable-resource
        cucumber.runtime.clj
        clojure.test)
  (:refer-clojure :exclude [resolve])
  (:require [clojure.java.jdbc :as jdbc]
            [ring.adapter.jetty :as jetty]
            [ring.util.response :as ring]
            [arche.media :as media]
            [clojurewerkz.urly.core :as urly]
            [arche.app-state :as app]
            [cheshire.core :refer :all :as json]
            [clj-http.client :as client]
            [environ.core :refer [env]])
  (:import [java.net URI URL]))

;; cucumber helpers
(defn table-to-map [table]
  (into {} (map vec (.raw table))))

(defn table-rows-map [table]
  (into {} (rest (table-to-map table))))

(def test-port 57767)

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

(defn url-to-test [path]
  (let [url (urly/url-like "http://localhost")
        with-port (.mutatePort url test-port)]
    (.toString (.mutatePath with-port path))))

(defn unexpected-response-message [url response]
  (format "expected successful response from %s: got %d, with body '%s'"
          url
          (:status response)
          (:body response)))

(defn execute-get-request [path headers]
  (client/get (url-to-test path) {:throw-exceptions false
                                  :headers headers}))

(defn execute-post-request [path headers body]
  (client/post (url-to-test path) {:throw-exceptions false
                                   :headers headers
                                   :body body}))

(defn call-app-url [url accept-type]
  (if (= (urly/host-of url) (urly/host-of (app/base-uri)))
    (let [path (urly/path-of (urly/url-like url))]
      (execute-get-request path {"Accept" accept-type}))
    (throw (Exception. (format "That wasn't an app url from the base-uri %s: %s"
                               (app/base-uri)
                               url)))))

(defn verify-app-url [url accept-type]
  (let [{status :status :as response} (call-app-url url accept-type)]
    (if (= 406 status)
      (verify-app-url url (ring/get-header response "Accept"))
      (is (= 200 status)
          (unexpected-response-message url response)))))

(def server (atom nil))

(defn server-start []
  (if @server
    (throw (IllegalStateException. "Server already started."))
    (reset! server (jetty/run-jetty handler
                                   {:port test-port
                                    :join? false}))))
(defn server-stop []
  (if (nil? @server)
    (throw (IllegalStateException. "Server already stopped."))
    (do
      (.stop @server)
      (reset! server nil))))

(def dbspec {:classname "com.mysql.jdbc.Driver"
             :subprotocol "mysql"
             :user (env :database-user)
             :password (env :database-password)
             :delimiters "`"
             :subname (format "//%s:3306/%s"
                              (env :database-host)
                              (env :database-name))})

(defn database-truncate []
  (jdbc/db-do-commands dbspec "TRUNCATE TABLE discoverable_resources;"))

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

(Then #"^I should get a response with the following errors:$" [arg1]
  (comment  Express the Regexp above with the code you wish you had  )
  (throw (cucumber.runtime.PendingException.)))
