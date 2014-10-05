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
   :invalid "is not valid"})

(defn- presence-valid? [value]
  (and (not (nil? value))
       (not (empty? value))))

(defn validate-presence [submitted]
  (if (presence-valid? submitted)
    []
    [:blank]))

(defn validate-format-fn [format-proc]
  (fn [value]
    (if (and (not (nil? value)) (format-proc value))
      []
      [:invalid])))

(defn validate-attribute [attribute submitted & validations]
  (let [value-to-test (get submitted attribute)]
    (if (empty? validations)
      {}
      (let [errors (apply
                    vector
                    (mapcat #(% value-to-test)
                            validations))]
        (if (empty? errors) {}
            {attribute errors})))))
