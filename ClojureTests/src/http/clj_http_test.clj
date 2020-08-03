;;; https://github.com/dakrone/clj-http
(ns http.clj_http_test
  (:require
   [clj-http.client :as client]))

(defn reportERROR [e]
   (println "here1")
   ;;(let [status e]
    (let [status (Throwable->map e)]
     (println "ERROR - HTTP Status: " status)
   )
  (println "here2")
  ) 

(defn GET [url] 
(try
  ;;(client/get url {:accept :json :throw-entire-message? true})
  (client/get url {:throw-entire-message? true})
  (catch Exception e (reportERROR e))
))

(GET "http://dummy.restapiexample.com/api/v1/employeesxxx")