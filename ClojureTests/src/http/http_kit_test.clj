
(ns http.http_kit_test
  (:require
   [clojure.data.json :as json]
   [org.httpkit.client :as client]
   [clojure.pprint :as pp]
   ))

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

;; Wrote this to select into a collection
;; BUT apparently it exists already (get-in m ks)
(defn select [result parts ]
  (reduce (fn [val x] (get val x)) result parts)
  )



(let [
      result (GET "http://dummy.restapiexample.com/api/v1/employees")
      num-items (count result)]
  ;(pp/pprint result)
  (println "Number of items:" num-items)
  (map #(get % "id") result)
)

(def testMap
  {:f1 {:f1.1 {:f1.1.1 "val :f1.1.1" :f1.1.2 "val :f1.1.2"}
        :f1.2 3}
   :f2 {:f2.1 {:f2.1.1 "val :f2.1.1"} :f2.2 "hello world"}}
  )

testMap