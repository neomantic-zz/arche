(ns wormhole-clj.db-spec
  (:use wormhole-clj.db)
  (:import java.util.Date java.sql.Timestamp)
  (:require [speclj.core :refer :all]
            [clj-time.coerce :as coerce]))

(describe
 "timestamping"
 (it "returns a sql timestamp"
     (should-be-a java.sql.Timestamp (sql-timestamp-now))))

(describe
 "time stamps for new records"
 (it "creates a map with the correct timestamps"
     (let [timestamps (new-record-timestamps)]
       (should= 2 (count timestamps))
       (should-not-be-nil {:created_at timestamps})
       (should-not-be-nil {:updated_at timestamps}))))
