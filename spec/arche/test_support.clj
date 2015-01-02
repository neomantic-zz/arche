;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;;  arche - A hypermedia resource discovery service
;;
;;  https://github.com/neomantic/arche
;;
;;  Copyright:
;;    2015
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

(ns arche.test-support
  (:require [clojure.java.jdbc :refer [db-do-commands]]
            [arche.config :refer :all]
            [arche.initialize :refer [seed-entry-point]]
            [arche.resources.discoverable-resource :refer [names]]))

(defn clean-database []
  (db-do-commands
   jdbc-dbspec
   (format "TRUNCATE TABLE %s;" (-> names :tableized name)))
  (seed-entry-point))

(defn truncate-database []
  (db-do-commands
   jdbc-dbspec
   (format "TRUNCATE TABLE %s;" (-> names :tableized name))))
