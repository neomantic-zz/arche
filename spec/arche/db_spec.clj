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

(ns arche.db-spec
  (:use arche.db)
  (:import java.util.Date java.sql.Timestamp)
  (:require [speclj.core :refer :all]
            [clj-time.coerce :as coerce]))

(describe
 "timestamping"
 (it "returns a sql timestamp"
     (should-be-a java.sql.Timestamp (sql-timestamp-now))))

(describe
 "fields"
 (it "returns a keys for its fields"
     (should== #{:created_at :updated_at} (into #{} timestamp-fields))))

(describe
 "time stamps for new records"
 (it "creates a map with the correct timestamps"
     (let [timestamps (new-record-timestamps)]
       (should= 2 (count timestamps))
       (should-not-be-nil {:created_at timestamps})
       (should-not-be-nil {:updated_at timestamps}))))
