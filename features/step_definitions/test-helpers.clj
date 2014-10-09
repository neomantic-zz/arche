(ns step-definitions.test-helpers
  (:use cucumber.runtime.clj
        clojure.test)
  (:require [arche.media :as media]
            [arche.core :as web]
            [ring.adapter.jetty :as jetty]
            [ring.util.response :as ring]
            [clojure.java.jdbc :as jdbc]
            [clojurewerkz.urly.core :as urly]
            [arche.app-state :as app]
            [clj-http.client :as client]
            [environ.core :refer [env]]))

(def test-port 57767)

(def last-response (atom nil))

(defn last-response-set! [new-response]
  (reset! last-response new-response))

(defn last-response-property [property]
  (fn [] (get @last-response property)))

(def last-response-body (last-response-property :body))
(def last-response-headers (last-response-property :headers))
(def last-response-status (last-response-property :status))

;; cucumber helpers
(defn table-to-map [table]
  (into {} (map vec (.raw table))))

(defn table-rows-map [table]
  (into {} (rest (table-to-map table))))

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
    (reset! server (jetty/run-jetty web/handler
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
