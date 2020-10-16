;;; Basics about Clojure functions:
;;; [1] Standard function definitions
;;; [1a] Private function - only visible in namespace
;;; [1b] Variadic Functions in Clojure
;;; [2] Anonymous function definitions
;;; [3] Recursive functions
;;; [3b] Recursive functions using loop...recur
;;; [4] Multi-Arity functions
;;; [4b] Argument deconstruction (in general)
;;; [5] Polymorphic Functions
;;; [6] Higher-order function 
;;; 
;;; References:
;;; http://clojure-doc.org/articles/language/functions.html
(ns functions.func-basics)


;;; [1] Standard function definitions
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

;;; [1b] Variadic Functions in Clojure
;;; i.e. functions with a variable number of arguments
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


;;; [1a] Private function - only visible in namespace
;; Making a function definition private to the name space
(defn- private-func []
  (prn "I am a very private funtion - only available within my own namespace"))

(private-func)


;;; [2] Anonymous function definitions
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


;;; [3] Recursive functions
;; Simple recursive function that counts down
(defn countdown1 [n]
  (prn n)
  (if (> n 0) (countdown1 (dec n)) nil))

  (countdown1 5)

;;; [3b] Recursive functions using loop...recur
;; Alternative recursive function that uses recur
;; Recur is more efficient than caling the function using standard recursion

;; This first example just uses recur (i.e. there is no loop)
;; the recur happens on the existing function
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


;;; [4] Multi-Arity functions
;;; You can define functions in clojure that accept different numbers of arguments and do
;;; something different depending on these arguments

(defn multi-arity-test
  ;; First definition is no args are passed
  ([] (prn "called with 0 args"))
  ([x](prn "called with 1 arg" x))
  ([x & args] (prn "called with many args" x args)))

(multi-arity-test 1 2 3 4)


;; Extra Arguments (aka Named Parameters)

(defn job-info
  [& {:keys [name job income] :or {job "unemployed" income "$0.00"}}]
  (if name  
    [name job income]
    (println "No name specified")))

  (job-info :name "Robert" :job "Engineer" :income "$100K")
  (job-info :job "Engineer")


;; [4b] Argument deconstruction (in general)

;; List/vector deconstruction
(defn decon-test1 [[x y & restArgs]]
  (prn "function takes a vector with 2 elements " x y restArgs ))

(decon-test1 [1 2 3 4])
(decon-test1 '(1 2 3 4))

;; You can also similar deconstruction in a let block
(defn decon-test2 [arg1]
  (let
   [[x y & restArgs] arg1]
    (prn "function takes a vector with 2 elements " x y restArgs)))

(decon-test2 [1 2])

;; Map deconstruction
(defn decon-test3 [{:keys [x y restArgs]}]
  (prn "function takes a vector with 2 elements " x y restArgs))

(decon-test3 {:x 33 :y 500 :restArgs [3 4 5 6]})

;; If you want to define explicit defaults if parameters are missing then
;; use the :or keyword in the deconstruction expression
(defn decon-test4 [{:keys [x y restArgs] :or {x -1 y -1 restArgs "EMPTY"}}]
  (prn "function takes a vector with 2 elements " x y restArgs))

(decon-test4 {:x 33 :y 500 :restArgs [3 4 5 6]})



;;; [5] Polymorphic Functions



;;; [6] Higher-order function 

;;; functional closures in clozure
;; A closure is a function that has access to some 
;; named value/variable outside its own scope, 
;; so from a higher scope surrounding the function 
;; when it was created (this excludes function arguments
;; and local named values created within the function)


(defn inc-by-n [n]
  ;; we return a function that is a closure. It has access to the
  ;; argument n that was passed into inc-by-n   
  (fn [x] (+ x n)))

(def inc11 (inc-by-n 11))

(inc11 3)

;; The standard function partial can be used to create some closures
;; https://clojuredocs.org/clojure.core/partial

(def inc11-v2 (partial + 11))
(inc11-v2 3)

;; Higher-order functions also refers to functions that take other
;; functions as arguments and apply those functions to datastructures (normally sequences) passed in
;; This includes the function filter, map, reduce, ...

;;; If you want more powerful argument deconstruction take a look at:
;;; https://github.com/clojusc/defun