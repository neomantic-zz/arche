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
  {:blank "can't be blank"
   :invalid "is not valid"
   :taken "is already taken"})

(defn validate-fn [boolean-proc symbol]
  (fn [submitted]
    (if (boolean-proc submitted)
      []
      [symbol])))

(def validate-presence
  (validate-fn
   (fn [submitted]
     (and (not (nil? submitted))
          (not (empty? submitted))))
   :blank))

(defn validate-format-fn [format-proc]
  (validate-fn
   (fn [value]
     (and (not (nil? value)) (format-proc value)))
   :invalid))

(defn validate-uniqueness-fn [proc]
  (validate-fn proc :taken))

(defn validates-attribute [attribute & validations]
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
(def errors-key :_*errors)

(defn has-errors? [entity]
  (not (nil? (get entity errors-key))))


(defn validate [attributes validations]
  (let [collected-errors (into {} (map #(% attributes) validations))]
    (if (empty? collected-errors)
      attributes
      (conj
       {errors-key collected-errors}
       attributes))))

(defn errors-get [errors-map]
  (errors-key errors-map))
