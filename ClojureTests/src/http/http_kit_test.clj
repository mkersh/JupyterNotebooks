
(ns http.http_kit_test
  (:require
   [clojure.data.json :as json]
   [org.httpkit.client :as client]
   [clojure.string :as str]
   [http.ENV :as env] ; This is the file to store env secrets in - make sure its in your .gitignore
   [clojure.pprint :as pp]))

(defn replacePlaceholder[currentStr placeHolder]
  (let [
        ;; Need to strip the {{ and }} from the placeholder before doing the lookup
        p2 (str/replace placeHolder "{{" "")
        p3 (str/replace p2 "}}" "")
        placeHolderValue (get env/ENV-MAP p3)
        ]
    (str/replace currentStr placeHolder placeHolderValue))
)
;; Support the expansion of placeholder in URLs 
;; e.g "{{env1}}/branches" which will have the {{env1}} placeholders replaced
(defn expandURL [url]
   (let [placeholderRegExp #"\{\{[^\}]*\}\}"
         placeholderList (re-seq placeholderRegExp url)
         placeholderSet (set placeholderList)]
     (reduce replacePlaceholder url placeholderSet)
     )
  )

(expandURL "{{env1}}/branches")

;; Testing string replace
(str/replace "112211331144" "11" "@@")

;; Tests to find out how to use clojure regExps to extract what I need
(def placeholderRegExp #"\{\{[^\}]*\}\}")
(re-seq placeholderRegExp "{{env1}}/branches{{env2}}some more{{env3}}")
(re-seq placeholderRegExp "{{env1}}")
(re-seq placeholderRegExp "{{env1}}{{env1}}")
(re-seq placeholderRegExp "no placeholder to expand")

(defn GET [url]
  (let [response @(client/get url {:accept :json})
        status (:status response)]

    (if (< status 300)
      (println "Successful Call: " status)
      (println "ERROR Status: " status))
    (get (json/read-str (:body response)) "data")))

;; Wrote this to select into a collection
;; BUT apparently it exists already (get-in m ks)
(defn select [result parts]
  (reduce (fn [val x] (get val x)) result parts))



(defn- test1 [attr]
  (let [result (GET "http://dummy.restapiexample.com/api/v1/employees")
        num-items (count result)]
    ;(pp/pprint result) ; This was pretty-printing the complete result
    (println "Number of items:" num-items)
    (map #(get % attr) result) ; This is now just selecting the top level "id" keys from the results
                               ; Need to make this more readable though by introducing a higher level function 
    ))

;; Running tests
;; TBD - Need to find the best way to do run unit-tests in Clojure

(comment
  ; Using  (comment ...) block so that the tests are not executed when loading the file
  ; BUT you can still execute tests from your REPL
(test1 "id") ; alt+ctrl+c space in Calva to execute
(test1 "employee_name")
)

;; The shape of the results returned from test1:
{"id" "19"
"employee_name" "Bradley Greer"
"employee_salary" "132000"
"employee_age" "41"
"profile_image" ""}

;; How to handle passing either a single attribute or a attr-list
;;     i.e. allow (test1 "id") or (test1 ["id" "other-attr"])
(defn handleSingleValueOrList [attr]
  (flatten (conj [] attr))) 

;; This method works but do we really want to be doing this throughout our code??
(handleSingleValueOrList "id")
(handleSingleValueOrList ["id","employee_name"])

;; Then I wondered if there was an easier way
(defn handleSingleValueOrList2 [& attr-list]
  attr-list)

(handleSingleValueOrList2 "id")
(handleSingleValueOrList2 ["id","employee_name"]) ; This doesn't work though :(
; You could just use the varargs capability and pass as the last set of args
; This is probbly the method I will use - until I'm told there's a better way in clojure
(handleSingleValueOrList2 "id","employee_name")

