(ns step-definitions.discoverable-resources-steps
  (:use wormhole-clj.core wormhole-clj.db cucumber.runtime.clj)
  (:require [clojure.java.jdbc :as jdbc]))

(defn table-to-map [table]
  (into {} (map vec (.raw table))))

(After []
       (jdbc/db-do-commands dbspec "TRUNCATE TABLE discoverable_resources;"))

(Given #"^a discoverable resource exists with the following attributes:$" [table]
       (let [table-map (table-to-map table)]
         (discoverable-resource-create
          (get table-map "resource_name")
          (get table-map "link_relation")
          (get table-map "href"))))

(When #"^I invoke the uniform interface method GET to \"([^\"]*)\" accepting \"([^\"]*)\"$" [arg1 arg2]
  (comment  Express the Regexp above with the code you wish you had  )
  (throw (cucumber.runtime.PendingException.)))

(Then #"^I should get a status of (\d+)$" [arg1]
  (comment  Express the Regexp above with the code you wish you had  )
  (throw (cucumber.runtime.PendingException.)))

(Then #"^the resource representation should have exactly the following properties:$" [arg1]
  (comment  Express the Regexp above with the code you wish you had  )
  (throw (cucumber.runtime.PendingException.)))

(Then #"^the resource representation should have exactly the following links:$" [arg1]
  (comment  Express the Regexp above with the code you wish you had  )
  (throw (cucumber.runtime.PendingException.)))
