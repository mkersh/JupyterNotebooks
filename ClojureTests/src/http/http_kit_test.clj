
;;; http://http-kit.github.io/
(ns http.http_kit_test
  (:require
   [clojure.data.json :as json]
   [org.httpkit.client :as client]
   [clojure.string :as str]
   [http.ENV :as env] ; This is the file to store env secrets in - make sure its in your .gitignore
   [clojure.pprint :as pp]))

(defn DEBUG [& args]
  ;(apply println args)
  )

(DEBUG "hello" 1 2 3 "Hello worlkd!!")

(defn get-auth [envId]
  (:basic-auth (get env/ENV-MAP envId)))

(defn replacePlaceholder [currentStr placeHolder]
  (let [;; Need to strip the {{ and }} from the placeholder before doing the lookup
        p2 (str/replace placeHolder "{{" "")
        p3 (str/replace p2 "}}" "")
        placeHolderValue (:url (get env/ENV-MAP p3))]
    (str/replace currentStr placeHolder placeHolderValue)))
;; Support the expansion of placeholder in URLs 
;; e.g "{{env1}}/branches" which will have the {{env1}} placeholders replaced
(defn expandURL [url]
  (let [placeholderRegExp #"\{\{[^\}]*\}\}"
        placeholderList (re-seq placeholderRegExp url)
        placeholderSet (set placeholderList)]
    (reduce replacePlaceholder url placeholderSet)))

(expandURL "{{env1}}/branches")

;; Testing string replace
(str/replace "112211331144" "11" "@@")

;; Tests to find out how to use clojure regExps to extract what I need
(def placeholderRegExp #"\{\{[^\}]*\}\}")
(re-seq placeholderRegExp "{{env1}}/branches{{env2}}some more{{env3}}")
(re-seq placeholderRegExp "{{env1}}")
(re-seq placeholderRegExp "{{env1}}{{env1}}")
(re-seq placeholderRegExp "no placeholder to expand")


(defn GET2 [url, options]
  (DEBUG "GET2: started")
  (let [url-expanded (expandURL url)
        ;options2 (assoc options :basic-auth basic-auth)
        response @(client/get url-expanded options)
        status (:status response)]
    (DEBUG "GET2: " url-expanded)
    (if (< status 300)
      (DEBUG "Successful Call: " status)
      (DEBUG "ERROR Status: " status))
    ;(DEBUG response)
    (json/read-str (:body response))))

(defn GET [url]
  (GET2 url, {}))

(defn PRINT [http-kit-response]
  (let [num-items (count http-kit-response)]
    (pp/pprint http-kit-response) ; This was pretty-printing the complete result
    (DEBUG "Number of items:" num-items)
    ;(map #(get % attr) result) ; This is now just selecting the top level "id" keys from the results
                               ; Need to make this more readable though by introducing a higher level function 
    ))

;;; -----------------------------------------------------------------
;;; Mambu API tests
;;; 

(defn apiTest-getAllBranches []
  (let [options {:basic-auth (get-auth "env1")
                 :headers {"Accept" "application/vnd.mambu.v2+json"}
                 :query-params {"detailsLevel" "FULL"}}]
    (PRINT (GET2 "{{env1}}/branches" options))))

;; Testing the functions
(comment
  (time (apiTest-getAllBranches)))


;;; Functions for examining JSON API results returned
(declare findPath-vector findPath-map search-leaf)

(defn search-map-item [keyValueItem matchStr accessPathList]
  (DEBUG "Search Map Item:" keyValueItem)
  (let
   [[k item] keyValueItem
    newAccessPathList (conj accessPathList k)]
    (cond
      (vector? item)
      (findPath-vector item matchStr newAccessPathList)

      (map? item)
      (findPath-map item matchStr newAccessPathList)
      :else
      (search-leaf k item matchStr newAccessPathList))))

(defn findPath-map [root matchStr accessPathList]
  (DEBUG "Searching in map")
  (map #(search-map-item % matchStr accessPathList)  root))

;;(map #(search-map-item % "john" [])  {:f1 1 :f2 "hello" :f3 "john"})


(defn search-leaf [key item matchStr accessPathList]
  (DEBUG "Search Leaf Node: " item " access path: " accessPathList)
  (if (or (.contains (str key) matchStr)
          (.contains (str item) matchStr))
    (do
      (DEBUG "*****Match Found****** " item "AccessPath: " accessPathList)
      accessPathList)
    nil))

(defn search-vec-item [indexedItem matchStr accessPathList]
  (DEBUG "Search Vector Item:" indexedItem)
  (let
   [[index item] indexedItem
    newAccessPathList (conj accessPathList index)]
    (cond
      (vector? item)
      (findPath-vector item matchStr newAccessPathList)

      (map? item)
      (findPath-map item matchStr newAccessPathList)
      :else
      (search-leaf nil item matchStr newAccessPathList))))


(defn findPath-vector [root matchStr accessPathList]
  (let
   [enumList (map-indexed vector root)]
    (DEBUG "Searching in Vector")
    (DEBUG enumList)
    (map #(search-vec-item % matchStr accessPathList)  enumList)))

(defn findPath-aux [root matchStr accessPathList]
  (DEBUG "root= " root)
  (cond
    (vector? root)
    (findPath-vector root matchStr accessPathList)

    (map? root)
    (findPath-map root matchStr accessPathList)
    :else
    (search-leaf nil root matchStr accessPathList)))

;; Given a resultEdn find a match to matchStr
;; Return the access-path to where the matchStr is in the resultEdn
;; NOTE: Likely use is to add "@@" into a key/value of the resultEdn

(declare get-lists-from tidyup-results)
(defn findPath [matchStr resultEdn]
  (get-lists-from (tidyup-results (findPath-aux resultEdn matchStr []))) ; last parm builds up the access-path
  )

(def testEdn ["ffff@@" {:fred "john"} {:gary 2 :hh ["john" 1 2 3 "john"]} "john"])

(declare get-lists-from)
(defn get-lists-items [res item]
  (cond
    (vector? item)
    (conj res item)
    (seq? item)
    (concat res (get-lists-from item))
    :else
    nil))

(defn get-lists-from [seq1]
  (reduce get-lists-items [] seq1))

(get-lists-from '(([1 :fred]) (([2 :hh 0] [2 :hh 4])) [3]))

(declare tidyup-results)
(defn tidyup-item [res item]
  (DEBUG "considering: " item (type item))
  (cond
    (or (vector? item) (seq? item))
    (do
      (DEBUG "vector or list")
      (let [recRes (tidyup-results item)]
        (if (or (nil? recRes) (empty? recRes))
          res
          (concat res [recRes]))))
    :else
    (do
      (DEBUG ":else " item)
      (if (nil? item)
        res
        (conj res item)))))

(defn tidyup-results [lst]
  (let [res (reduce tidyup-item [] lst)]
    (DEBUG "START tidyup-results " lst)
    (DEBUG "TIDY: " res)
    res))

(tidyup-results [[nil 5 6] [1 '(7 8 9)] 2 nil 3 4])
(tidyup-results ['(nil (nil nil nil nil nil)) '(nil) 0])
(tidyup-results ['(nil)])


(list? '(nil))

(comment

  (let [res (findPath "john" testEdn )
        ;flRes (get-lists-from (tidyup-results res))
        ]
  ;; There appears to be some lazy evaluation going on here
  ;; The next lines DEBUG gets mixed up with the evaluation of res
    (DEBUG "RESULTS:" res)
    res))


;; Wrote this to select into a collection
;; BUT apparently it exists already (get-in m ks)
(defn select [result parts]
  (reduce (fn [val x] (get val x)) result parts))

(defn- test1 [attr]
  (let [result (get (GET "http://dummy.restapiexample.com/api/v1/employees") "data")
        num-items (count result)]
    ;(pp/pprint result) ; This was pretty-printing the complete result
    (DEBUG "Number of items:" num-items)
    (map #(get % attr) result) ; This is now just selecting the top level "id" keys from the results
                               ; Need to make this more readable though by introducing a higher level function 
    ))

;; Running tests
;; TBD - Need to find the best way to do run unit-tests in Clojure

(comment
  ; Using  (comment ...) block so that the tests are not executed when loading the file
  ; BUT you can still execute tests from your REPL
  (test1 "id") ; alt+ctrl+c space in Calva to execute
  (test1 "employee_name"))







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

