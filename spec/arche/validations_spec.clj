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

;; (describe
;;  "without validations"
;;  (it "returns nothing when no validation"
;;      (should= [] (validate-attribute :an-attribute ""))))

(describe
 "presence validation"
 (it "returns an array with :blank when validation fails"
     (should== {:an-attribute [:blank]}
               ((validates-attribute
                 :an-attribute
                 validate-presence)
                {:an-attribute ""})))
 (it "returns an array when blank value is nil"
     (should== {:an-attribute [:blank]}
               ((validates-attribute
                 :an-attribute
                 validate-presence)
                {:an-attribute nil})))
 (it "returns an empty hash when value passes validation"
     (should== {} ((validates-attribute
                    :an-attribute
                    validate-presence)
                   {:an-attribute "ggg"}))))

(describe
 "multilple validations"
 (it "returns correct array of keys when multiple validations present fail"
     (should== {:an-attribute  [:blank :fake]}
               ((validates-attribute :an-attribute
                                     validate-presence
                                     (fn [submitted] [:fake]))
                {:an-attribute ""})))
 (it "returns correct array of keys one multiple validations fails"
     (should== {:an-attribute  [:fake]}
               ((validates-attribute :an-attribute
                                     validate-presence
                                     (fn [submitted] [:fake]))
                {:an-attribute "shsnhsnhg"}))) )
(describe
 "error message"
 (it "returns the correct message for :blank"
     (should= "can't be blank" (:blank default-error-messages)))
 (it "returns the correct message for :blank"
     (should= "is not valid" (:invalid default-error-messages))))
