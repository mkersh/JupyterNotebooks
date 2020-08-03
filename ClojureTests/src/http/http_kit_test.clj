
(ns http.http_kit_test
  (:require
   [clojure.data.json :as json]
   [org.httpkit.client :as client]
   [clojure.pprint :as pp]
   ))

(defn reportERROR [e]
  (println "here1")
   ;;(let [status e]
  (let [status (Throwable->map e)]
    (println "ERROR - HTTP Status: " status))
  (println "here2"))

(defn GET [url]
  (let [response @(client/get url {:accept :json})
        status (:status response)]
    
    (if (< status 300 )  
      (println "Successful Call: " status)
      (println "ERROR Status: " status)
    )
    (get (json/read-str (:body response)) "data")
    )
)

(let [
      result (GET "http://dummy.restapiexample.com/api/v1/employees")
      num-items (count result)
]
  ;(pp/pprint result)
  (println "Number of items:" num-items)
  (map #(get % "id") result)
)