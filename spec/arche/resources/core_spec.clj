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

(ns arche.resources.core-spec
  (:use arche.resources.core)
  (:require [speclj.core :refer :all]))

(describe
 "creating error maps"
 (it "creates a error map when attribute has one error"
     (should= {:errors
               {:an-attribute ["can't be blank"]}}
              (construct-error-map {:an-attribute [:blank]}
                                   {:blank "can't be blank"})))
 (it "creates a error map when attribute has multiple-errors"
     (should= {:errors
               {:an-attribute ["can't be blank"
                               "is already taken"]}}
              (construct-error-map {:an-attribute [:blank :taken]}
                                   {:blank "can't be blank"
                                    :taken "is already taken"})))
 (it "creates a error map when more than one attribute has a error"
     (should= {:errors
               {:an-attribute ["can't be blank"]
                :another ["can't be blank"]}}
              (construct-error-map {:an-attribute [:blank]
                                    :another [:blank]}
                                   {:blank "can't be blank"}))))
