;; migrations/20140907113742788-discoverable-resources-timestamps.clj

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

(defn up []
  ["ALTER TABLE discoverable_resources ADD COLUMN (`created_at` DATETIME NOT NULL, `updated_at` DATETIME NOT NULL)"])

(defn down []
  ["ALTER TABLE discoverable_resources DROP COLUMN created_at"
   "ALTER TABLE discoverable_resources DROP COLUMN updated_at"])
