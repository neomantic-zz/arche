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

(ns arche.validations-spec
  (:use arche.validations)
  (:require [speclj.core :refer :all]))

(describe
 "without validations"
 (it "returns nothing when no validation"
     (should= [] (validate-attribute :an-attribute ""))))

(describe
 "presence validation"
 (it "returns an array with values with validation"
     (should== [:blank] (validate-attribute :an-attribute "" validate-presence))))

(describe
 "multilple validations"
 (it "returns correct array of keys when multiple validations present"
     (should== [:blank :fake]
               (validate-attribute :an-attribute "http://sthnth"
                                   validate-presence
                                   (fn [attribute submitted] [:fake])))))
(describe
 "error message"
 (it "returns the correct message for :blank"
     (should= "can't be blank" (:blank default-error-messages))))
