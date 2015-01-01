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

(ns arche.config-spec
  (:require [speclj.core :refer :all]
            [arche.config :refer :all]))

(describe
 "cache-expiry"
 (it "returns 600 as its default"
     (should= 600 cache-expiry)))

(describe
 "base-uri"
 (it "returns the correct base-uri"
     (should-not-be-nil base-uri)))

(describe
 "port"
 (it "returns 5000 as its default"
     (should= 5000 port)))
