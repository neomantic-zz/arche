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

(def ^:private prev-page-key :prev-page)
(def ^:private next-page-key :next-page)

(defn page-predicate-fn [key]
  (fn [paginated]
    (> (get paginated key) 0)))

(def has-prev-page? (page-predicate-fn prev-page-key))
(def has-next-page? (page-predicate-fn next-page-key))

(defn calculate-offset [page per-page default-per-page]
  (cond
   (<= per-page 0) 0
   (= default-per-page 0) 0
   :else (let [offset (- (* per-page (dec page)) 2)]
           (cond
            (< offset 0) 0
            (> offset default-per-page) (- (* default-per-page (dec page)) 2)
            :else offset))))

(defn calculate-limit [requested-per-page default-per-page]
  (cond
   (<= requested-per-page 0) 0
   (and (> requested-per-page 0) (<= default-per-page 0)) 0
   :else  (+ 1 (if (> requested-per-page default-per-page)
             default-per-page
             requested-per-page))))

(defn window-has-prev? [window-total page-number]
  (if (<= page-number 1) false
    (> window-total 0)))

(defn window-has-next? [window-total page-number limit]
  (cond
   (or (<= page-number 0) (= limit 0) (< window-total 0)) false
   (and (= limit 1) (<= window-total 1)) false
   (= page-number 1) (>= window-total limit)
   :else (> window-total limit)))

(defn records [items page limit]
  (cond
   (empty? items) items
   (= page 1) (if (< (count items) limit)
                items
                (apply vector (drop-last items)))
   :else
   (let [remainder (drop 1 items)]
     (if (<= (count items) limit)
       remainder
       (apply vector (drop-last remainder))))))

(defn paginate-fn1 [fetcher-fn default-per-page]
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
             limit (calculate-limit per-page default-per-page)
             items (fetcher-fn offset limit)
             window-total (count items)
             has-prev (window-has-prev? window-total page)
             has-next (window-has-next? window-total page limit)]
         ;;(prn (format "offset %d" offset))
         {prev-page-key has-prev
          next-page-key has-next
          :records (records items page limit)})))))

(defn calculate-offset1 [page per-page]
  (* (dec page) per-page))

(defn calculate-limit1 [page requested-per-page default-per-page]
  (inc (if (or (< requested-per-page 0) (> requested-per-page default-per-page))
         default-per-page
         requested-per-page)))

(defn paginate-fn [fetcher-fn default-per-page]
  (fn paginate
    ([] (paginate 1))
    ([page] (paginate page default-per-page))
    ([page per-page]
       ;; algorithm works like this: get 1 more than the maximum count (peek)
       ;; and if amount returns match, then there is a next-page
       (let [offset (calculate-offset1 page default-per-page)
             limit (calculate-limit1 page per-page default-per-page)
             records (fetcher-fn offset limit)
             has-prev (if (and (= (count records) 0) (> page 1))
                        false
                        (not (= page 1)))
             has-next (or (> (count records) per-page)  ;;when, I wanted a specific per_page, and there were more
                          (> (count records) default-per-page))]
         {prev-page-key (if has-prev (dec page) 0)
          next-page-key (if has-next (inc page) 0)
          :records (if has-next
                     (drop-last (apply vector records))
                     (apply vector records))}))))
