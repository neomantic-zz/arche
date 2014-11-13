(ns arche.paginate-test
  (:require [clojure.test :refer :all]
            [arche.paginate :refer :all]))

(deftest calculate-offset-test
  (is (= 0 (calculate-offset -1 0 0)))
  (is (= 0 (calculate-offset -1 1 0)))
  (is (= 0 (calculate-offset -1 1 25)))

  (is (= 0 (calculate-offset 0 0 0)))
  (is (= 0 (calculate-offset 0 0 25)))

  (is (= 0 (calculate-offset 0 1 0)))
  (is (= 0 (calculate-offset 0 1 -5)))

  (is (= 0 (calculate-offset 0 -1 0)))
  (is (= 0 (calculate-offset 0 1 -25)))
  (is (= 0 (calculate-offset 0 2 -25)))

  (is (= 0 (calculate-offset 1 0 0)))
  (is (= 0 (calculate-offset 1 1 0)))
  (is (= 0 (calculate-offset 1 1 25)))
  (is (= 0 (calculate-offset 1 1 -25)))

  (is (= 0 (calculate-offset 1 -1 0)))
  (is (= 0 (calculate-offset 1 -1 25)))
  (is (= 0 (calculate-offset 1 -2 25)))

  (is (= 0 (calculate-offset 1 3 0)))
  (is (= 0 (calculate-offset 1 3 2)))
  (is (= 0 (calculate-offset 1 3 3)))
  (is (= 0 (calculate-offset 1 3 4)))

  (is (= 0 (calculate-offset 2 0 0)))
  (is (= 0 (calculate-offset 2 -1 0)))
  (is (= 0 (calculate-offset 2 -1 25)))

  (is (= 0 (calculate-offset 2 1 0)))
  (is (= 0 (calculate-offset 2 -1 0)))
  (is (= 0 (calculate-offset 2 -1 25)))
  (is (= 0 (calculate-offset 2 1 25)))
  (is (= 1 (calculate-offset 2 3 25)))
  (is (= 23 (calculate-offset 2 25 25)))
  (is (= 23 (calculate-offset 2 68 25))))

(deftest calculate-limit-test
  ;;calculate-limit [requested-per-page default-per-page]
  (is (= 0 (calculate-limit 0 0)))
  (is (= 0 (calculate-limit -1 0)))
  (is (= 0 (calculate-limit -2 0)))
  (is (= 0 (calculate-limit 0 1)))
  (is (= 0 (calculate-limit 0 2)))
  (is (= 0 (calculate-limit 0 -1)))
  (is (= 0 (calculate-limit 0 -2)))
  (is (= 2 (calculate-limit 1 1)))
  (is (= 2 (calculate-limit 3 1)))
  (is (= 3 (calculate-limit 3 2))))

(deftest window-has-prev?-test
  (is (= false (window-has-prev? -1 1)))
  (is (= false (window-has-prev? 0 1)))
  (is (= false (window-has-prev? 1 0)))
  (is (= false (window-has-prev? 1 -1)))
  (is (= false (window-has-prev? 1 1)))
  (is (= true  (window-has-prev? 1 2))))

(deftest window-has-next?-test
  ;; less that zero page
  (is (= false (window-has-next? 0 -1 0)))
  (is (= false (window-has-next? 8 -1 0)))
  (is (= false (window-has-next? 8 -1 2)))

  ;; zero page
  (is (= false (window-has-next? 0 0 0)))
  (is (= false (window-has-next? 8 0 2)))

  ;; page one
  (is (= false (window-has-next? -1 1 -1)))
  (is (= false (window-has-next? -1 1 0)))
  (is (= false (window-has-next? -1 1 1)))
  (is (= false (window-has-next? -1 1 2)))
  (is (= false (window-has-next? 0 1 1)))
  (is (= false (window-has-next? 1 1 0)))

  (is (= false (window-has-next? 1 1 1)))

  (is (= true (window-has-next? 2 1 1)))
  (is (= true (window-has-next? 4 1 1)))

  ;; ;; page greatr that 1
  (is (= false (window-has-next? -1 2 -1)))
  (is (= false (window-has-next? -1 2 0)))
  (is (= false (window-has-next? -1 2 1)))
  (is (= false (window-has-next? -1 2 2)))
  (is (= false (window-has-next? 0 2 1)))

  (is (= false (window-has-next? 1 2 0)))
  (is (= false (window-has-next? 1 2 1)))

  (is (= true (window-has-next? 4 1 4)))
  (is (= false (window-has-next? 2 2 2)))

  )

(deftest records-test
  ;;(records vector page limit)
  (is (= []    (records [] -1 -1)))
  (is (= []    (records [] -1 0)))
  (is (= []    (records [] 0 1)))

  (is (= []    (records [] 1 -1)))
  (is (= []    (records [] 1 0)))
  (is (= []    (records [] 1 2)))
  (is (= [1]   (records [1] 1 2)))
  (is (= [1]   (records [1 2] 1 2)))
  (is (= [1 2] (records [1 2 3] 1 2)))

  (is (= [2 3] (records [1 2 3] 2 4))))

(deftest paginate-without-page
  (is (= {:prev-page false :next-page false :records []}
         ((paginate-fn1
           (fn [page per-page] [])
           -1))))
  (is (= {:prev-page false :next-page false :records []}
         ((paginate-fn1
           (fn [page per-page] [])
           0))))
  (is (= {:prev-page false :next-page false :records []}
         ((paginate-fn1
           (fn [page per-page] [])
           -1))))
  (is (= {:prev-page false :next-page false :records []}
         ((paginate-fn1
           (fn [page per-page] [])
           0))))
  (is (= {:prev-page false :next-page false :records [1]}
         ((paginate-fn1
           (fn [page per-page] [1])
           25))))
  (is (= {:prev-page false :next-page false :records [1]}
         ((paginate-fn1
           (fn [page per-page] [1])
           25))))
  (is (= {:prev-page false :next-page false :records []}
         ((paginate-fn1
           (fn [page per-page] [1])
           0))))
  (is (= {:prev-page false :next-page false :records [1]}
         ((paginate-fn1
           (fn [page per-page] [1])
           3))))
  (is (= {:prev-page false :next-page false :records [1 2 3]}
         ((paginate-fn1
           (fn [page per-page] [1 2 3])
           3))))
  (is (= {:prev-page false :next-page true :records [1 2 3]}
         ((paginate-fn1
           (fn [page per-page] [1 2 3 4])
           3))))
  )

(deftest paginate-with-negative
  (is (= {:prev-page false :next-page false :records []}
         ((paginate-fn1
           (fn [page per-page] [])
           -1) -1)))
  (is (= {:prev-page false :next-page false :records []}
         ((paginate-fn1
           (fn [page per-page] [])
           0) -1)))
  (is (= {:prev-page false :next-page false :records []}
         ((paginate-fn1
           (fn [page per-page] [1])
           1) -1)))
  (is (= {:prev-page false :next-page false :records []}
         ((paginate-fn1
           (fn [page per-page] [])
           25) -1)))
  (is (= {:prev-page false :next-page false :records []}
         ((paginate-fn1
           (fn [page per-page] [1])
           -1) -1)))
  (is (= {:prev-page false :next-page false :records []}
         ((paginate-fn1
           (fn [page per-page] [1])
           0) -1)))
  (is (= {:prev-page false :next-page false :records []}
         ((paginate-fn1
           (fn [page per-page] [1])
           25) -1)))
  (is (= {:prev-page false :next-page false :records []}
         ((paginate-fn1
           (fn [page per-page] [1])
           3) 0)))
  (is (= {:prev-page false :next-page false :records []}
         ((paginate-fn1
           (fn [page per-page] [1 2 3])
           3) -1)))
  (is (= {:prev-page false, :next-page false, :records []}
         ((paginate-fn1
           (fn [page per-page] [1 2 3 4])
            3) -1))))

(deftest paginate-with-page-zero
  (is (= {:prev-page false :next-page false :records []}
         ((paginate-fn1
           (fn [page per-page] [])
           -1) 0)))
  (is (= {:prev-page false :next-page false :records []}
         ((paginate-fn1
           (fn [page per-page] [])
           0) 0)))
  (is (= {:prev-page false :next-page false :records []}
         ((paginate-fn1
           (fn [page per-page] [])
           -1) 0)))
  (is (= {:prev-page false :next-page false :records []}
         ((paginate-fn1
           (fn [page per-page] [1])
           25) 0)))
  (is (= {:prev-page false :next-page false :records []}
         ((paginate-fn1
           (fn [page per-page] [1])
           0) 0)))
  (is (= {:prev-page false :next-page false :records []}
         ((paginate-fn1
           (fn [page per-page] [1])
           3) 0)))
  (is (= {:prev-page false :next-page false :records []}
         ((paginate-fn1
           (fn [page per-page] [1 2 3])
           3) 0)))
  (is (= {:prev-page false, :next-page false, :records []}
         ((paginate-fn1
           (fn [page per-page] [1 2 3 4])
            3) 0)))
  )

(deftest paginate-with-page-1
  (is (= {:prev-page false :next-page false :records []}
         ((paginate-fn1
           (fn [page per-page] [])
           -1) 1)))
  (is (= {:prev-page false :next-page false :records []}
         ((paginate-fn1
           (fn [page per-page] [])
           0) 1 )))
  (is (= {:prev-page false :next-page false :records []}
         ((paginate-fn1
           (fn [page per-page] [])
           -1) 1)))
  (is (= {:prev-page false :next-page false :records []}
         ((paginate-fn1
           (fn [page per-page] [])
           0) 1)))
  (is (= {:prev-page false :next-page false :records [1]}
         ((paginate-fn1
           (fn [page per-page] [1])
           25) 1)))
  (is (= {:prev-page false :next-page false :records []}
         ((paginate-fn1
           (fn [page per-page] [1])
           0) 1)))
  (is (= {:prev-page false :next-page false :records [1]}
         ((paginate-fn1
           (fn [page per-page] [1])
           3) 1)))
  (is (= {:prev-page false :next-page false :records [1 2 3]}
         ((paginate-fn1
           (fn [page per-page] [1 2 3])
           3) 1)))
  (is (= {:prev-page false, :next-page true, :records [1 2 3]}
         ((paginate-fn1
           (fn [page per-page] [1 2 3 4])
            3) 1))))


(deftest paginate-with-page-greater-than-one
  (is (= {:prev-page false :next-page false :records []}
         ((paginate-fn1
           (fn [page per-page] [])
           -1) 2)))
  (is (= {:prev-page false :next-page false :records []}
         ((paginate-fn1
           (fn [page per-page] [])
           0) 2)))
  (is (= {:prev-page false :next-page false :records []}
         ((paginate-fn1
           (fn [page per-page] [])
           1) 2)))
  (is (= {:prev-page true :next-page false :records [3]}
         ((paginate-fn1
           (fn [page per-page]
             ;;exists
             ;; [1 2 3]
             ;; with paginated
             [2 3])
           2) 2)))
  (is (= {:prev-page true :next-page false :records [3]}
         ((paginate-fn1
           (fn [page per-page]
             ;;exists
             ;; [1 2 3]
             ;; with paginated
             [2 3])
           2) 2)))
  (is (= {:prev-page true :next-page false :records [3 4]}
         (do
           (prn "the test")
           ((paginate-fn1
             (fn [page per-page]
               ;;exists
               ;; [1 2 3 4]
               ;; with paginated
               [2 3 4])
             2) 2))))
  (is (= {:prev-page true, :next-page true, :records [3 4]}
         ((paginate-fn1
           (fn [page per-page]
             ;; exists
             ;;[1 2 3 4 5]
             ;; but paginated, returns
             [2 3 4 5])
           2) 2)))
  (is (= {:prev-page true, :next-page false, :records [4 5]}
         ((paginate-fn1
           (fn [page per-page]
             ;; exists
             ;;[1 2 3 4 5]
             ;; but paginate, returns
             [3 4 5])
           3) 2)))
  ;; (is (= {:prev-page true, :next-page true, :records [4 5]}
  ;;        ((paginate-fn1
  ;;          (fn [page per-page]
  ;;            ;; exists
  ;;            ;;[1 2 3 4 5 6]
  ;;            ;; but paginate, returns
  ;;            [3 4 5 6])
  ;;          3) 2)))
  )
