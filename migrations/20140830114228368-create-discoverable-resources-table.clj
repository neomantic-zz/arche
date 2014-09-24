;; migrations/20140830114228368-create-discoverable-resources-table.clj

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
  ["CREATE TABLE discoverable_resources(id INT(11) NOT NULL AUTO_INCREMENT PRIMARY KEY, resource_name VARCHAR(255) NOT NULL, link_relation VARCHAR(255) NOT NULL, href VARCHAR(255) NOT NULL)"])

(defn down []
  ["DROP TABLE discoverable_resources"])
