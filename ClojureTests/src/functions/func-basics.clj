;;; Basics about Clojure functions
(ns functions.func-basics)


;; Defining a simple function which takes no arguments
(defn test-func []
(prn "test-func"))

;; To call it pass as the 1st argument in a list data struct
(test-func)

;; test-func is a var that is bound to the function
test-func


(defn test-func2 [x y]
  (prn "test-func2" x  y ))


(test-func2 4 5)


(defn test-func3
  "Same as the previous test-func2 but contains this meta doc string"
  [x y]
  (prn "test-func3" x  y))

(test-func3 4 5)

(defmacro docstring [symbol]
  `(:doc (meta (var ~symbol))))

(docstring test-func3)

(defn test-func4
  "function with 1 mandatory arg and any number of optional"
  [x & args]
  (prn "test-func4" x  args))

(test-func4 1 2 3 4 5 6)

(defn my-add
  "function with 1 mandatory arg and any number of optional"
  [& args]
  (apply + args)) ;; this is how you can apply a list of args to a function

(my-add 1 2 3 4 5 6 7 8 9 10)

(def ARGS-LIST [1 2 3 4 5])

;; you can pass multiple individual values to apply as well
(apply +  10 20 ARGS-LIST )

;; This next would not work. Only the last parameter can be a list/collection
;;(apply +  10 [2 3 4] ARGS-LIST)


;; following creates an anonymous function
(fn [x]
   (prn "Function called with" x))

;; This creates an anonymous function and then calls it
((fn [x]
  (prn "Function called with" x)) 5)


;; following binds an anonymous function to a variable
;; which is the equivalent of doing (defn)
(def func6 (fn [x]
  (prn "Function called with" x)))

(func6 1)

(def func7 #(prn "another form of anonymous function" %))

(func7 7)

;; Abbreviated anon func with 2 args
(def func8 #(prn "another form of anonymous function" %2 %1))

(func8 7 8)


;; Simple recursive function that counts down
(defn countdown1 [n]
  (prn n)
  (if (> n 0) (countdown1 (dec n)) nil))

  (countdown1 5)

;; Alternative recursive function that uses recur
;; Recur is more efficient than caling the function using standard recursion
(defn countdown2 [n]
  (prn n)
  (if (> n 0) (recur (dec n)) nil))

(countdown2 5)

;; This next one uses the full form of loop..recur
;; the loop is like an anonymous function that is then recursed on
;; when you call recur
(defn countdown3 [n]
  (prn "This will only get called once")
  (loop [m n]
    (prn m)
    (if (> m 0) (recur (dec m)) nil)))

(countdown3 5)