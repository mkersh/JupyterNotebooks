;;; Many sequences in Clojure are Lazy - http://clojure-doc.org/articles/language/laziness.html
;;; 
(ns data-types.lazy-seq.lazy-tests
;;   (:require
;;    [clojure.pprint :as pp]
;;    )
  )


(import java.util.UUID)

(defn uuid-seq
  []
  (lazy-seq
   (cons (str (UUID/randomUUID))
         (uuid-seq))))

;; The next line proves that take returns a lazy seq as weel
(take 100000000 (uuid-seq))

;; Try and force a large sequence and it will then take time
(dorun (take 1000 (uuid-seq)))


;; This is a very elegant way to specify the Fibonaaci sequence
(defn fib-seq
  "Returns a lazy sequence of Fibonacci numbers"
  ([]
   (fib-seq 0 1))
  ([a b]
   (lazy-seq
    (cons b (fib-seq b (+ a b))))))

(take 20 (fib-seq))

(defn inf-range
  ([]
   (inf-range 0))
  ([n]
   (lazy-seq
    (cons n (inf-range (+ n 1))))))

(defn printall [lst]
  (dorun (map prn lst) )
  )

(printall (take 100 (inf-range)))

(inf-range)

;; My inf-range is the same as the official range
(range 0 1000 3)

;; I thought the next function would go into an infinite loop
;; but it doesn't. The cons seems to be automatically workimng in a 
;; lazy seq way
(defn inf-range-wrong
  ([]
   (inf-range 0))
  ([n]
    (cons n (inf-range (+ n 1)))))

(printall (take 200 (inf-range-wrong)))