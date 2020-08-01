;;; Namespace names need to match file name
;;; Required namespaces need to be on the classpath
;;;     - This is a bit messy for my tests
(ns ns2
   (:require (ns1))
  )

;; Print the classpath
;
(println (seq (.getURLs (java.lang.ClassLoader/getSystemClassLoader))))

 ;(require '(ns1))

(defn ns2SayHello []
  (println "MKNameSpace2 Says Hello from me AND!!!")
  (ns1/ns1SayHello)
  )


(ns2SayHello)
 ;; => Syntax error compiling at (namespaces/ns2.clj:1:8244).
 ;;    Unable to resolve symbol: ns2SayHello in this context

*ns*