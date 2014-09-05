(ns step-definitions.discoverable-resources-steps
  (:use wormhole-clj.core wormhole-clj.db cucumber.runtime.clj clojure.test)
  (:require [clojure.java.jdbc :as jdbc]
            [ring.adapter.jetty :as ring]
            [clj-http.client :as client]))

(defn table-to-map [table]
  (into {} (map vec (.raw table))))

(def test-port 57767)

(declare last-response)

(defmacro with-server [app options & body]
  `(let [server# (ring/run-jetty ~app ~(assoc options :join? false))]
     (try
       ~@body
       (finally (.stop server#)))))

(After []
       (jdbc/db-do-commands dbspec "TRUNCATE TABLE discoverable_resources;"))

(Given #"^a discoverable resource exists with the following attributes:$" [table]
       (let [table-map (table-to-map table)]
         (discoverable-resource-create
          (get table-map "resource_name")
          (get table-map "link_relation")
          (get table-map "href"))))

(When #"^I invoke the uniform interface method GET to \"([^\"]*)\" accepting \"([^\"]*)\"$" [path media-type]
      (with-server handler {:port test-port}
        (def last-response (client/get (format "%s:%d/%s" "http://localhost" test-port path)
                                       {:headers {"Accept" media-type}}))))

(Then #"^I should get a status of (\d+)$" [status]
      (assert (= (:status last-response) status)))

(Then #"^the resource representation should have exactly the following properties:$" [arg1]
      (is (= (:body last-response) "what")))

(Then #"^the resource representation should have exactly the following links:$" [arg1]
      (is (= (:body last-response) "what")))
