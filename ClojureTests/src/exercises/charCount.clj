;;; Write a function that given an input string like "aaabbbaaacccbdddd" returns
;;; (["a",6], ["b", 4], ["c", 3], ["d", 4])
(ns charCount)


(defn- add-char-to-map [countMap ch]
  (let [currVal (get countMap ch)]
    ;; could have done the count here but doinf a conj likely to be faster
    (assoc countMap ch (conj currVal ch)))
    )

(defn- count-chars [it]
  (vector (str (first it)) (count (second it))))

(defn count-str-chars [str-to-count]
  (map count-chars (reduce add-char-to-map {} str-to-count)))

;; could have used the standard partition-by function to achieve what I have doen in the 
;; reduce:
;; (partition-by identity "aaabbbccc")



(comment

(count-str-chars "aaabbbaaaabbbcccdddfffgggrrr")

)