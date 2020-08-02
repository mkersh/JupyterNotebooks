;;; Namespace names need to match file name
;;; Required namespaces need to be on the classpath
;;;     - This is a bit messy for my tests
(ns namespaces.ns2
   (:require [namespaces.ns1 :as req1])
  )
;; Here's another way of requiring another namespace
;; but you are better off using :require within (ns ...)
;(require '[namespaces.ns1 :as req1])

;; Print the classpath
(defn printClassPath []
(println (seq (.getURLs (java.lang.ClassLoader/getSystemClassLoader)))))

;(printClassPath)

(defn ns2SayHello []
  (println "MKNameSpace2 Says Hello from me AND!!!")
  (req1/ns1SayHello)
  )


(ns2SayHello)
 ;; => Syntax error compiling at (namespaces/ns2.clj:1:8244).
 ;;    Unable to resolve symbol: ns2SayHello in this context

*ns*