;;; https://docs.google.com/document/d/1tiz0qjGfqv0On5k0R3EeIdOgf_9ZZovqcW8yPb5QhCs/edit
;;; 
(ns algorithms.puzzles.8queens
;;   (:require
;;    [clojure.pprint :as pp]
;;    )
  )



(declare permute2)

;; loop-through-items:
;; From permute2 this function is called to loop through all the remain
;; items. 
;; 
(defn loop-through-items [res-list it new-perm remain]
  (let [new-remain (remove #(= % it) remain)]
    ;; Remove it from the remain list and add it to the new-perm we are building
    ;; When the new-perm is finished (i.e. no more remain items) we will add to the res-list 
    (permute2 new-remain (conj new-perm it) res-list)
    )
)

(defn permute2 [remain new-perm res-list]
  (if (empty? remain)
    (conj res-list new-perm)
    (reduce #(loop-through-items %1 %2 new-perm remain) res-list remain)))

(defn permute 
  "Given a sequence 2 return all the permutations of the items"
  [s]
  (permute2 s [] []) 
  )

(defn enumerate [s]
  (map #(vector %1 %2) s (range))
  )

(defn diag-check [f res-set col-it]
  (let [row-pos (first col-it)
        col-pos (second col-it)]
    (conj res-set (f row-pos col-pos))))

(defn queens-on-same-diagonals? [perm]
  (let [enum-perm (enumerate perm)
        set1 (reduce #(diag-check + %1 %2) #{} enum-perm)
        set2 (reduce #(diag-check - %1 %2) #{} enum-perm)
        c (count perm)]
    ;;(prn "set 1: " set1)
    ;;(prn "set 2: " set2)
    (if (= c (count set1)(count set2))
      true
      false)
    ))

(permute (range 8))

(queens-on-same-diagonals? [2 4 6 0 3 1 7 5])

(defn eight-queens []
  (let [perm-list (permute (range 8))]
    (filter queens-on-same-diagonals?  perm-list))
  )

(count (eight-queens))

(time (count (eight-queens)))
