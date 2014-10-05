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
               (validate-attribute
                :an-attribute
                {:an-attribute ""} validate-presence)))
 (it "returns an array when blank value is nil"
     (should== {:an-attribute [:blank]} (validate-attribute
                         :an-attribute
                         {:an-attribute nil}
                         validate-presence)))
 (it "returns an empty hash when value passes validation"
     (should== {} (validate-attribute
                   :an-attribute
                   {:an-attribute "ggg"}
                   validate-presence))))

(describe
 "multilple validations"
 (it "returns correct array of keys when multiple validations present fail"
     (should== {:an-attribute  [:blank :fake]}
               (validate-attribute :an-attribute ""
                                   validate-presence
                                   (fn [submitted] [:fake]))))
 (it "returns correct array of keys one multiple validations fails"
     (should== {:an-attribute  [:fake]}
               (validate-attribute :an-attribute
                                   {:an-attribute "shsnhsnhg"}
                                   validate-presence
                                   (fn [submitted] [:fake])))) )
(describe
 "error message"
 (it "returns the correct message for :blank"
     (should= "can't be blank" (:blank default-error-messages))))
