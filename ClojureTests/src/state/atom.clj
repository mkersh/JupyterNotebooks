;;; https://clojure.org/reference/atoms
(ns state.atom)


;; NOTE: memoize already exists in clojure.core BUT here is how it can be implemented
(defn memoize [f]
  (let [mem (atom {})]
    (fn [& args]
      (if-let [e (find @mem args)]
        (val e)
        (let [ret (apply f args)]
          (swap! mem assoc args ret)
          ret)))))

(defn fib [n]
  (if (<= n 1)
    n
    (+ (fib (dec n)) (fib (- n 2)))))

(time (fib 35))

(def fib (memoize fib))

(time (fib 35))
