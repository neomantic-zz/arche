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

(ns arche.paginate)

(def ^:private prev-page-key :has-prev)
(def ^:private next-page-key :has-next)

(defn page-predicate-fn [key]
  (fn [paginated]
    (> (get paginated key) 0)))

(def has-prev-page? (page-predicate-fn prev-page-key))
(def has-next-page? (page-predicate-fn next-page-key))

(defn- calculate-page-number-fn [page-predicate dec-or-inc]
  (fn [paginated requested-page]
    (if (page-predicate paginated)
      (dec-or-inc requested-page)
      requested-page)))

(def calculate-next-page-number (calculate-page-number-fn has-next-page? inc))
(def calculate-prev-page-number (calculate-page-number-fn has-prev-page? dec))

(defn calculate-offset [page per-page default-per-page]
  (if (or (<= per-page 0) (= default-per-page 0) (<= page 1))
    0
    (let [number-of (if (> per-page default-per-page)
                      default-per-page
                      per-page)]
      (dec (* (dec page) number-of)))))

(defn calculate-limit [page requested-per-page default-per-page]
  (cond
   (or (<= requested-per-page 0) (<= page 0))  0
   (and (> requested-per-page 0) (<= default-per-page 0)) 0
   :else (let [requested (if (> requested-per-page default-per-page)
                           default-per-page
                           requested-per-page)]
           (if (= page 1)
             (+ 1 requested)
             (+ 2 requested)))))

(defn window-has-prev? [window-total page-number]
  (if (<= page-number 1) false
    (> window-total 0)))

(defn window-has-next? [window-total page-number limit]
  (cond
   (or (<= page-number 0) (= limit 0) (< window-total 0)) false
   (and (= limit 1) (<= window-total 1)) false
   (= page-number 1) (>= window-total limit)
   :else (>= window-total limit)))

(defn records [items page limit]
  (cond
   (empty? items) items
   (= page 1) (if (< (count items) limit)
                items
                (apply vector (drop-last items)))
   :else
   (let [remainder (drop 1 items)]
     (if (< (count items) limit)
       remainder
       (apply vector (drop-last remainder))))))

(defn paginate-fn [fetcher-fn default-per-page]
  (fn paginate
    ([] (paginate 1))
    ([page] (paginate page default-per-page))
    ([page per-page]
       ;; algorithm works like this: get 1 more than the maximum count (peek)
       ;; and if amount returns match, then there is a next-page
       (if (or (<= page 0)
               (<= per-page 0)
               (<= default-per-page 0))
         {prev-page-key false
          next-page-key false
          :records []}
         (let [offset (calculate-offset page per-page default-per-page)
               limit (calculate-limit page per-page default-per-page)
               items (fetcher-fn offset limit)
               window-total (count items)
               has-prev (window-has-prev? window-total page)
               has-next (window-has-next? window-total page limit)]
           {prev-page-key has-prev
            next-page-key has-next
            :records (records items page limit)})))))
