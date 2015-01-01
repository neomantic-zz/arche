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

(ns arche.app-state-spec
  (:use arche.app-state)
  (:require [speclj.core :refer :all]))

(describe
 "paths"
 (it "returns the correct string for the alps path"
     (should= "alps" alps-path)))

(describe
 "creating urls"
 (it "creates the correct url for an alps profile"
     (should= "http://example.org/alps/FooBars"
              (alps-profile-url "FooBars")))
 (it "creates the correct url for an alps profile when resource name needs to be escaped"
     (should= "http://example.org/alps/Foo%20Bars"
              (alps-profile-url "Foo Bars"))))
