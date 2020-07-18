;;; ************************************************************
;;; 
;;; Clojure Tests and examples
;;;
;;;  GITHUB: https://github.com/mkersh/JupyterNotebooks/blob/master/ClojureTests/ClojureNotebook.clj
;;; 
;;; ************************************************************
(ns ClojureNotebook
  (:refer-clojure))
; (in-ns 'ClojureNotebook.newton-raphson)
; (ns-name *ns*)
; (ns-interns *ns*)
; (all-ns)


;;; *********************************  List datastructures
(def MyExampleList '(1 2 3 4 5 6))
MyExampleList

; adding to the beginning of a list
(cons 0 MyExampleList)

; add multiple items to the beginning of a list
(conj MyExampleList 7 8 9 10)

;; append 2 lists together
(reduce conj MyExampleList '(7 8 9 10))

;; Functions that you can apply to list
(nth MyExampleList 3)

;; This is an example of calling a java function .indexOf on a list
(.indexOf MyExampleList 4)


;;; ********************************* Vectors
;;; Very similar to lists but it general use vectors to store data. 
;;; One advantage is that you do not have to quote a vector when it just has data in it, 
;;; whereas with a list you do. 
;;; With a list (unless quoted) the first argument is always expected to be a function to apply the following arguments.

[1 2 3 4]

(vector 1 4 5 6)

(vector 1 2 "hh")

(comment
  ; Commenting next line out cos it causes an error
(vector-of :int 1 2 "hh")
)

(vector-of :int 1 2 3 4 5)

(vector-of :char \h \k)

(def myVector ["this" "that" "here" "there"])

(count myVector)

;; List comprehenxion
(for [x myVector]
  (str "1" x))

;; Modify a vector - Replace an element with a new one
(assoc myVector 1 "Wow")


;;; *************************************** Maps
(def myMap {:key1 "This is key1" :key2 "this is key 2"
            :key3 "This is key3"
            4 "this is keyed by 4"
            "SomeKey" "Value of somekey"})

myMap

;; There are a number of ways to get stuff out of a map
(println "Using get without a default: " (get myMap 4))
(println "Using get with a default: " (get myMap 5 "(optional) default if key missing"))
(println "Map as function: " (myMap "SomeKey"))
(println "Map as function (with default): " (myMap "SomeKey4" "default"))
(println "keyword as function " (:key30 myMap))

(def myMap2 (sorted-map :key55 ["55 val1"] :key56 ["55 val2"]))

(assoc myMap2 :key55 11)

;; Keys in maps are unique. If you want to add multiple values for a key you need to do something like:
(defn addItem 
  "this is a docString associated with the function"
  [key val]
  (let
   [curVal (myMap2 key)
    newVal (conj curVal val)]
    (def myMap2 (assoc myMap2 key newVal))))  ; Not a good idea I know!

(addItem :key55 90)

myMap2

(ns-name *ns*)


;;; *********************************  Functions
;;; 

;; Standard public function
(defn publicFunction
  "optional doc string"
  {:optional-meta-data 1}
  [arg1 arg2]
  (println (str "this is" arg1 arg2))
)

(publicFunction 1 2)

;; defn- creates a private function that is only visible within  the current namespace
(defn- privateFunction
  "optional doc string"
  {:optional-meta-data 1}
  [arg1 arg2]
  (println (str "this is" arg1 arg2)))

(privateFunction 1 2)

(defn funcWithOverloadedArgs
  ([]
   (println "No Args passed"))
  ([arg1]
   (println "1 Args passed"))
 ([arg1 & rest]
   (println "More than 1 Args passed " rest)))

(funcWithOverloadedArgs 1 2 3 )

(defn funcWithListParam
  ;([[]]
   ;(println "Null list"))
  ([[arg1 & rest]]
   (println (str "Args passed list " arg1 rest))
  ))

(funcWithListParam [])

(def my-vector [1 2 3])

(let [[x & z] my-vector]
  (println x z))

;; Anonymous functions

(def fn1
 (fn [x] (println x))) 

(fn1 "this is it")

;; Shorter version of anonymous functions
(#(println (str "anon func called with " %)) 1)

(def fn2 #(println (str "anon func2 called with " %)))

(fn2 "Let see this work")

;; Recursive functions

(defn fac [x]
  (if (= x 1)
    1
    (* x (fac (- x 1)))
    )
  )

(fac 100N)

(defn fac2 [x]
  (loop [n x
         result 1]
    (if (= n 1)
      result
      (recur (- n 1) (* result n) )
      )
    ))


(fac2 100N)

;; ->> Macro

(->> 1 (+ 2) (fn2))


