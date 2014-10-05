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

(ns arche.http-spec
  (:require [speclj.core :refer :all]
            [arche.http :refer :all]))


(describe
 "building headers"
 (it "returns a map with the correct key for the location header"
     (should= {"Location" "a-url"}
              (header-location "a-url")))
 (it "returns a map with the correct key for the accept header"
     (should= {"Accept" "a-content-type"}
              (header-accept "a-content-type")))
 (it "returns a map with the correct key for a cache control header"
     (should= {"Cache-Control" "some value"}
              (header-cache-control "some value")))
 (it "returns a map with the correct key for a etags"
     (should= {"ETag" "some value"}
              (header-etag "some value")))
 (it "returns a private cache controller header map with max age"
     (should= {"Cache-Control" "max-age=600, private"}
              (cache-control-header-private-age 600)))
 (it "returns a content-type header"
     (should= {"Content-Type" "application/vnd.github"}
              (header-content-type "application/vnd.github"))))
