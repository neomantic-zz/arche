;; migrations/20140907113742788-discoverable-resources-timestamps.clj

(defn up []
  ["ALTER TABLE discoverable_resources ADD COLUMN (`created_at` DATETIME NOT NULL, `updated_at` DATETIME NOT NULL)"])

(defn down []
  ["ALTER TABLE discoverable_resources DROP COLUMN created_at"
   "ALTER TABLE discoverable_resources DROP COLUMN created_at"])
