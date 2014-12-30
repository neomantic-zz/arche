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

(ns arche.validations)

(def default-error-messages
  "Human-readable failure messages for the blank, invalid, and taken failures"
  {:blank "can't be blank"
   :invalid "is not valid"
   :taken "is already taken"})

(defn validate-fn
  "Given a symbol representation its failure message, and a function that
  accepts one parameter andreturns boolean value, returns
  a function that applies the boolean-proc to the submitted data, and returns
  a empty vector the value passes inspection, else the vector populated
  with the failure key"
  [boolean-proc symbol]
  (fn [submitted]
    (if (boolean-proc submitted)
      []
      [symbol])))

(def validate-presence
  "A function that tests for 'presence' - if the submitted value is
   nil or empty...if so, then validation fails and [:blank] is returned"
  (validate-fn
   (fn [submitted]
     (and (not (nil? submitted))
          (not (empty? submitted))))
   :blank))

(defn validate-format-fn
  "Given a function that tests for the format of a item, returns a function
  that will return [:invalid] if the format fails,
  [:invalid] if the applicaton of the format function returns false"
  [format-fn]
  (validate-fn
   (fn [value]
     (and (not (nil? value)) (format-fn value)))
   :invalid))

(defn validate-uniqueness-fn
  "Accepts a function that tests for a items uniqueness, and when
  applied to a submitted value returns [:taken] when it fails the
  uniqueness test."
  [fn]
  (validate-fn fn :taken))

(defn validates-attribute
  "Given key which is expected in a hash-map and a set of validation
  functions, tests the submitted value associated with the key.
  If all validations pass, returns an empty hash-map, else it returns
  hash-map with a vector associated with key that contains all the
  failure keywords."
  [attribute & validations]
  (fn [all-attributes]
    (if (empty? validations)
      {}
      (let [errors (apply
                    vector
                    (mapcat #(% (get all-attributes attribute))
                            validations))]
        (if (empty? errors) {}
            {attribute errors})))))

;; a key which most likely will not conflict with
;; the attributes of a column name in a table
(def errors-key
  "The key used to annotate a hash-map that has errors"
  :_*errors)

(defn has-errors?
  "Returns a boolean indicating if the hash-map has the errors-key"
  [entity]
  (not (nil? (get entity errors-key))))


(defn validate
  "Given a hash map of attribute key words and value, apply all
  apply a vector of validations (functions of validate-attributes). If
  any validation fails, annotate the hash-map with an error keys, otherwise
  returns the submited hash-map."
  [attributes validations]
  (let [collected-errors (into {} (map #(% attributes) validations))]
    (if (empty? collected-errors)
      attributes
      (conj
       {errors-key collected-errors}
       attributes))))

(defn errors-get
  "Returns the collection of errors"
  [errors-map]
  (errors-key errors-map))
