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
  {:blank "can't be blank"})

(defn- presence-valid? [value]
  (and (not (nil? value))
       (not (empty? value))))

(defn validate-presence [attribute submitted]
  (if (presence-valid? (get submitted attribute))
    []
    [:blank]))

(defn validate-format-fn [format-proc]
  (fn [attribute value]
    (if (format-proc value)
      []
      [:format])))

(defn validate-attribute [attribute values & validations]
  (if (empty? validations)
    []
    (apply concat (map #(% attribute values)
                       validations))))
