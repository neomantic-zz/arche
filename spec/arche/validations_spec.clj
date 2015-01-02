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
 "validate-presence"
 (it "returns correct key when submitted value is empty"
     (should== [:blank] (validate-presence {})))
 (it "returns correct key when submitted value is empty string"
     (should== [:blank] (validate-presence "")))
 (it "returns correct key when submitted value is nil"
     (should== [:blank] (validate-presence nil))))

(describe
 "validate-uniqueness"
 (it "returns correct key when uniqueness fails"
     (should== [:taken] ((validate-uniqueness-fn
                          (fn [attribute]
                            false)) "")))
 (it "returns an empty vector key when uniqueness succeeds"
     (should== [] ((validate-uniqueness-fn
                          (fn [attribute]
                            true)) ""))))

(describe
 "validate-format"
 (it "returns correct key when uniqueness fails"
     (should== [:invalid] ((validate-format-fn
                          (fn [attribute]
                            false)) "")))
 (it "returns an empty vector key when uniqueness succeeds"
     (should== [] ((validate-format-fn
                          (fn [attribute]
                            true)) ""))))

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
                                     (fn [_] [:fake]))
                {:an-attribute ""})))
 (it "returns correct array of keys one multiple validations fails"
     (should== {:an-attribute  [:fake]}
               ((validates-attribute :an-attribute
                                     validate-presence
                                     (fn [_] [:fake]))
                {:an-attribute "shsnhsnhg"}))))
(describe
 "error message"
 (it "returns the correct message for :blank"
     (should= "can't be blank" (:blank default-error-messages)))
 (it "returns the correct message for :invalid"
     (should= "is not valid" (:invalid default-error-messages)))
 (it "returns the correct message for :taken"
     (should= "is already taken" (:taken default-error-messages))))

(describe
 "validate"
 (it "returns map with error key and attributes when it fails and there is only one validation"
     (should== {:an-attribute ""
                :_*errors {:an-attribute [:blank]}}
               (validate {:an-attribute ""}
                [(validates-attribute :an-attribute validate-presence)])))
 (it "returns map with error key and attributes when it fails and there is only multple validation"
     (should== {:an-attribute "", :another "blah"
                :_*errors {:an-attribute [:blank]
                           :another [:fake]
                           }}
               (validate {:an-attribute "" :another "blah"}
                [(validates-attribute :an-attribute validate-presence)
                 (validates-attribute :another (validate-fn (fn [_] false) :fake))])))
 (it "returns map with no errors when no validations are passed"
     (should== {:an-attribute ""}
               (validate {:an-attribute ""} [])))
 (it "returns map with no errors when all validations succeeed"
     (should== {:an-attribute ""}
               (validate {:an-attribute ""}
                          [(validates-attribute :an-attribute
                                                (validate-fn (fn [_] true) :fake))]))))
