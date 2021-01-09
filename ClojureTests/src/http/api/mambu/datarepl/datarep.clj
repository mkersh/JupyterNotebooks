;;; Examples/tests for how to replicate data/objects from Mambu to a data-lake/DWH
(ns http.api.mambu.datarepl.datarep
  (:require [http.api.json_helper :as api]
            [http.api.api_pipe :as steps]))


(defn getTimer []
(. System (nanoTime)))

(defn showTimeDiff [title startTimer]
  (let [curr-time (. System (nanoTime))
        time-diff (/ (- curr-time startTimer) 1000000.0)]
        (prn title time-diff )
        time-diff))

(defn get-all-clients-next-page [context]
  (let [page-size (:page-size context)
        offset (* (:page-num context) page-size)]
    {:url (str "{{*env*}}/clients")
     :method api/GET
     :query-params {"detailsLevel" "FULL"
                    "paginationDetails" "ON"
                    "offset" offset "limit" (:page-size context)
                    "sortBy" "lastModifiedDate:ASC"} ;; ASC is important to min changes to what the cursor returns
     :headers {"Accept" "application/vnd.mambu.v2+json"
               "Content-Type" "application/json"}}))

(comment 
(api/setenv "env2")

(def context {:page-size 2 :page-num 0})

;; Test getting a page of customer objects
(let [startTimer (getTimer)
      context1 (steps/apply-api get-all-clients-next-page context :allcust_page)
      timeDiff (showTimeDiff "API call took (ms):" startTimer)
      page (:allcust_page context1)]

  (api/PRINT (api/extract-attrs ["id" "creationDate" "lastModifiedDate"] page))
  (prn "API call took (ms):" timeDiff)
  (showTimeDiff "Time including print (ms):" startTimer)
  )




;; Test that showTimeDiff is accurate
(let [startTimer (getTimer)]
(Thread/sleep 2000)
(showTimeDiff "Sleep Timer (ms):" startTimer)
)


)

