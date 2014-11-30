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
  (is (= 2 (calculate-offset 2 3 25)))

  (is (= 24 (calculate-offset 2 25 25)))
  (is (= 24 (calculate-offset 2 68 25))))

(deftest calculate-limit-test
  ;;(calculate-limit [page requested-per-page default-per-page])
  (is (= 0 (calculate-limit -1 0 0)))
  (is (= 0 (calculate-limit -1 1 0)))
  (is (= 0 (calculate-limit -1 1 2)))
  (is (= 0 (calculate-limit -1 2 2)))

  (is (= 0 (calculate-limit 0 -1 0)))
  (is (= 0 (calculate-limit 0 1 0)))
  (is (= 0 (calculate-limit 0 1 2)))
  (is (= 0 (calculate-limit 0 2 2)))

  (is (= 0 (calculate-limit 1 -1 0)))
  (is (= 0 (calculate-limit 1 -1 1)))
  (is (= 0 (calculate-limit 1 -1 2)))

  (is (= 0 (calculate-limit 1 0 0)))
  (is (= 0 (calculate-limit 1 1 -1)))

  (is (= 2 (calculate-limit 1 1 1)))
  (is (= 2 (calculate-limit 1 1 2)))
  (is (= 3 (calculate-limit 1 3 2)))
  (is (= 3 (calculate-limit 1 4 2)))

  (is (= 3 (calculate-limit 2 1 1)))
  (is (= 3 (calculate-limit 2 1 2)))
  (is (= 4 (calculate-limit 2 3 2)))
  (is (= 4 (calculate-limit 2 4 2))))

(deftest calculate-limit-test1
  (is (= 26 (calculate-limit 2 24 25))))

(deftest window-has-prev?-test
  (is (= false (window-has-prev? -1 1)))
  (is (= false (window-has-prev? 0 1)))
  (is (= false (window-has-prev? 1 0)))
  (is (= false (window-has-prev? 1 -1)))
  (is (= false (window-has-prev? 1 1)))
  (is (= true  (window-has-prev? 1 2))))

(deftest window-has-next?-testg
  (is (= true (window-has-next? 26 2 26))))

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

  ;; page greater that 1
  (is (= false (window-has-next? -1 2 -1)))
  (is (= false (window-has-next? -1 2 0)))
  (is (= false (window-has-next? -1 2 1)))
  (is (= false (window-has-next? -1 2 2)))
  (is (= false (window-has-next? 0 2 1)))

  (is (= false (window-has-next? 1 2 0)))
  (is (= false (window-has-next? 1 2 1)))

  ;;(is (= false (window-has-next? 2 2 2)))

  (is (= true (window-has-next? 4 1 4))))

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
  (is (= [2 3] (records [1 2 3] 2 4)))
  (is (= [1 2 3 4 5 6 7 8 9 10
          11 12 13 14 15 16 17 18 19 20
          21 22 23 24 25]
         (records [0 1 2 3 4 5 6 7 8 9 10
                   11 12 13 14 15 16 17 18 19 20
                   21 22 23 24 25 26] 2 26))))

(deftest paginate-without-page
  (is (= {:has-prev false :has-next false :records []}
         ((paginate-fn
           (fn [page per-page] [])
           -1))))
  (is (= {:has-prev false :has-next false :records []}
         ((paginate-fn
           (fn [page per-page] [])
           0))))
  (is (= {:has-prev false :has-next false :records []}
         ((paginate-fn
           (fn [page per-page] [])
           -1))))
  (is (= {:has-prev false :has-next false :records []}
         ((paginate-fn
           (fn [page per-page] [])
           0))))
  (is (= {:has-prev false :has-next false :records [1]}
         ((paginate-fn
           (fn [offset limit]
             [1])
           25))))
  (is (= {:has-prev false :has-next false :records [1]}
         ((paginate-fn
           (fn [page per-page] [1])
           25))))
  (is (= {:has-prev false :has-next false :records []}
         ((paginate-fn
           (fn [page per-page] [1])
           0))))
  (is (= {:has-prev false :has-next false :records [1]}
         ((paginate-fn
           (fn [page per-page] [1])
           3))))
  (is (= {:has-prev false :has-next false :records [1 2 3]}
         ((paginate-fn
           (fn [page per-page] [1 2 3])
           3))))
  (is (= {:has-prev false :has-next true :records [1 2 3]}
         ((paginate-fn
           (fn [page per-page]
             [1 2 3 4])
           3)))))

(deftest paginate-with-negative
  (is (= {:has-prev false :has-next false :records []}
         ((paginate-fn
           (fn [page per-page] [])
           -1) -1)))
  (is (= {:has-prev false :has-next false :records []}
         ((paginate-fn
           (fn [page per-page] [])
           0) -1)))
  (is (= {:has-prev false :has-next false :records []}
         ((paginate-fn
           (fn [page per-page] [1])
           1) -1)))
  (is (= {:has-prev false :has-next false :records []}
         ((paginate-fn
           (fn [page per-page] [])
           25) -1)))
  (is (= {:has-prev false :has-next false :records []}
         ((paginate-fn
           (fn [page per-page] [1])
           -1) -1)))
  (is (= {:has-prev false :has-next false :records []}
         ((paginate-fn
           (fn [page per-page] [1])
           0) -1)))
  (is (= {:has-prev false :has-next false :records []}
         ((paginate-fn
           (fn [page per-page] [1])
           25) -1)))
  (is (= {:has-prev false :has-next false :records []}
         ((paginate-fn
           (fn [page per-page] [1])
           3) 0)))
  (is (= {:has-prev false :has-next false :records []}
         ((paginate-fn
           (fn [page per-page] [1 2 3])
           3) -1)))
  (is (= {:has-prev false, :has-next false, :records []}
         ((paginate-fn
           (fn [page per-page] [1 2 3 4])
            3) -1))))

(deftest paginate-with-page-zero
  (is (= {:has-prev false :has-next false :records []}
         ((paginate-fn
           (fn [page per-page] [])
           -1) 0)))
  (is (= {:has-prev false :has-next false :records []}
         ((paginate-fn
           (fn [page per-page] [])
           0) 0)))
  (is (= {:has-prev false :has-next false :records []}
         ((paginate-fn
           (fn [page per-page] [])
           -1) 0)))
  (is (= {:has-prev false :has-next false :records []}
         ((paginate-fn
           (fn [page per-page] [1])
           25) 0)))
  (is (= {:has-prev false :has-next false :records []}
         ((paginate-fn
           (fn [page per-page] [1])
           0) 0)))
  (is (= {:has-prev false :has-next false :records []}
         ((paginate-fn
           (fn [page per-page] [1])
           3) 0)))
  (is (= {:has-prev false :has-next false :records []}
         ((paginate-fn
           (fn [page per-page] [1 2 3])
           3) 0)))
  (is (= {:has-prev false, :has-next false, :records []}
         ((paginate-fn
           (fn [page per-page] [1 2 3 4])
            3) 0)))
  )

(deftest paginate-with-page-1
  (is (= {:has-prev false :has-next false :records []}
         ((paginate-fn
           (fn [page per-page] [])
           -1) 1)))
  (is (= {:has-prev false :has-next false :records []}
         ((paginate-fn
           (fn [page per-page] [])
           0) 1 )))
  (is (= {:has-prev false :has-next false :records []}
         ((paginate-fn
           (fn [page per-page] [])
           -1) 1)))
  (is (= {:has-prev false :has-next false :records []}
         ((paginate-fn
           (fn [page per-page] [])
           0) 1)))
  (is (= {:has-prev false :has-next false :records [1]}
         ((paginate-fn
           (fn [page per-page] [1])
           25) 1)))
  (is (= {:has-prev false :has-next false :records []}
         ((paginate-fn
           (fn [page per-page] [1])
           0) 1)))
  (is (= {:has-prev false :has-next false :records [1]}
         ((paginate-fn
           (fn [page per-page] [1])
           3) 1)))
  (is (= {:has-prev false :has-next false :records [1 2 3]}
         ((paginate-fn
           (fn [page per-page] [1 2 3])
           3) 1)))
  (is (= {:has-prev false, :has-next true, :records [1 2 3]}
         ((paginate-fn
           (fn [page per-page] [1 2 3 4])
            3) 1))))


(deftest paginate-with-page-greater-than-one
  (is (= {:has-prev false :has-next false :records []}
         ((paginate-fn
           (fn [page per-page] [])
           -1) 2)))
  (is (= {:has-prev false :has-next false :records []}
         ((paginate-fn
           (fn [page per-page] [])
           0) 2)))
  (is (= {:has-prev false :has-next false :records []}
         ((paginate-fn
           (fn [page per-page] [])
           1) 2)))
  (is (= {:has-prev true :has-next false :records [3]}
         ((paginate-fn
           (fn [page per-page]
             ;;exists
             ;; [1 2 3]
             ;;with paginated
             [2 3])
           2) 2)))
  (is (= {:has-prev true :has-next false :records [3]}
         ((paginate-fn
           (fn [page per-page]
             ;;exists
             ;;[1 2 3]
             ;;with paginated
             [2 3])
           2) 2)))
  (is (= {:has-prev true :has-next false :records [3 4]}
         (do
           ((paginate-fn
             (fn [page per-page]
               ;;exists
               ;;[1 2 3 4]
               ;;with paginated
               [2 3 4])
             2) 2))))
  (is (= {:has-prev true, :has-next true, :records [3 4]}
         ((paginate-fn
           (fn [page per-page]
             ;; exists
             ;;[1 2 3 4 5]
             ;; but paginated, returns
             [2 3 4 5])
           2) 2)))
  (is (= {:has-prev true, :has-next false, :records [4 5]}
         ((paginate-fn
           (fn [page per-page]
             ;;exists
             ;;[1 2 3 4 5]
             ;;but paginate, returns
             [3 4 5])
           3) 2)))
  (is (= {:has-prev true, :has-next false, :records [4 5 6]}
         ((paginate-fn
           (fn [page per-page]
             ;;exists
             ;;[1 2 3 4 5 6]
             ;;but paginate, returns
             [3 4 5 6])
           3) 2)))
  (is (= {:has-prev true, :has-next true, :records [4 5 6]}
         ((paginate-fn
           (fn [page per-page]
             ;; exists
             ;;[1 2 3 4 5 6]
             ;; but paginate, returns
             [3 4 5 6 7])
           3) 2)))
  )

(deftest with-request-count1
  (is (= {:has-prev true,
          :has-next true,
          :records [25 26 27 28 29 30 31 32 33 34
                    35 36 37 38 39 40 41 42 43 44
                    45 46 47 48]}
         ((paginate-fn
           (fn [offset limit]
             [24
              25 26 27 28 29 30 31 32 33 34
              35 36 37 38 39 40 41 42 43 44
              45 46 47 48
              49]) 25)
          2 24))))

(deftest test-has-next?
  (is (= true (has-next-page? {:has-next true})))
  (is (= false (has-next-page? {:has-next false}))))

(deftest test-has-next?
  (is (= true (has-prev-page? {:has-prev true})))
  (is (= false (has-prev-page? {:has-prev false}))))

(deftest next-page-number
  (is (= 3 (calculate-next-page-number {:has-next true} 3)))
  (is (= 4 (calculate-next-page-number {:has-next true} 3))))
