;; Helper functions to calling JSON APIs using http://http-kit.github.io/
;; GitHub: https://github.com/mkersh/JupyterNotebooks/blob/master/ClojureTests/src/http/api/json_helper.clj 
;;
;; Pretty print directly (clojure.pprint/pprint
(ns http.api.json_helper
  (:require
   [clojure.data.json :as json]
   [org.httpkit.client :as client]
   [clojure.string :as str]
   [http.ENV :as env] ; This is the file to store env secrets in - make sure its in your .gitignore
   [clojure.pprint :as pp]))

(declare expandURL expand-options best-response request GET2)

;;; ----------------------------------------------------------------------
;;; JSON - Helper Methods:
;;; 
;;; GET, POST, DELETE, PATCH, PUT
;;; 

(defn GET
  ([url] (GET2 url, {}))
  ([url, options] (GET2 url, options)))

(defn POST [url, options]
  (request url, client/post, options))

(defn PATCH [url, options]
  (request url, client/patch, options))

(defn DELETE [url, options]
  (request url, client/delete, options))

(defn PUT [url, options]
  (request url, client/put, options))

(defn PRN [str1 & options ]
  (if (:no-print (first options))
    nil
    (prn str1)
    ))

(defn- request [url, method, options0]
  (let [url-expanded (expandURL url)
        options (expand-options options0)
        response @(method url-expanded options)
        status (:status response)]

    (if (< status 300)
      (PRN (str "Successful Call: " status) options0)
      (if (:throw-errors options)
        (throw (Exception. (str "ERROR Status: " status)))
        (prn "ERROR Status: " status)))
    (best-response response)))

(defn- GET2 [url, options]
  (request url, client/get, options))

;;; ----------------------------------------------------------------------
;;; Print and Extraction helper functions
;;; 
;;; 


(defn PRINT [http-kit-response & options]
  (if (:no-print (first options))
    http-kit-response
    (let [num-items (count http-kit-response)]
      (pp/pprint http-kit-response) ; This was pretty-printing the complete result
      (prn "Number of items:" num-items))))


(defn get-auth [envId]
  (:basic-auth (get env/ENV-MAP envId)))

(declare get-lists-from tidyup-results findPath-aux)

(defn find-path 
  "Given a resultEdn find a match to matchStr
   Return the access-path to where the matchStr is in the resultEdn
   NOTE: Likely use is to add '@@' into a key/value of the resultEdn"
  [matchStr resultEdn]
  (assert (string? matchStr)) ; make sure a String has been passed
  (get-lists-from (tidyup-results (findPath-aux resultEdn matchStr []))) ; last parm builds up the access-path
  )

(declare get-obj-attrs)

(defn extract-attrs 
  "Extract the attributes defined in attrList from the objOrList passed in"
  [attrList objOrList]
  (let [objList ; make sure objOrList is a vector
        (if (vector? objOrList)
          objOrList
          (vector objOrList))]
    (map #(get-obj-attrs % attrList) objList)))

(defn docstring [symbol]
  (:doc (meta (resolve symbol))))

;;; ----------------------------------------------------------------------
;;; Internal helper functions
;;; 
;;; 

(defn- get-obj-attrs [obj attrList]
  (let [attrValList (map #(vector % (get obj %)) attrList)]
    (into {} attrValList)))

;;; Functions for examining JSON API results returned
(declare findPath-vector findPath-map search-leaf)
(defn- DEBUG [& args]
  (identity args) ; To prevent clj-kondo warning
  ;(apply println args)
  )

(defn- search-map-item [keyValueItem matchStr accessPathList]
  (DEBUG "Search Map Item:" keyValueItem)
  (let
   [[k item] keyValueItem
    newAccessPathList (conj accessPathList k)]
    (cond
      (vector? item)
      (concat (findPath-vector item matchStr newAccessPathList)
              (search-leaf k nil matchStr newAccessPathList))

      (map? item)
      (findPath-map item matchStr newAccessPathList)
      :else
      (search-leaf k item matchStr newAccessPathList))))

(defn- findPath-map [root matchStr accessPathList]
  (DEBUG "Searching in map")
  (map #(search-map-item % matchStr accessPathList)  root))


(defn- search-leaf [key item matchStr accessPathList]
  (DEBUG "Search Leaf Node: " item " access path: " accessPathList)
  (if (or (.contains (str key) matchStr)
          (.contains (str item) matchStr))
    (do
      (DEBUG "*****Match Found****** " item "AccessPath: " accessPathList)
      accessPathList)
    nil))

(defn- search-vec-item [indexedItem matchStr accessPathList]
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


(defn- findPath-vector [root matchStr accessPathList]
  (let
   [enumList (map-indexed vector root)]
    (DEBUG "Searching in Vector")
    (DEBUG enumList)
    (map #(search-vec-item % matchStr accessPathList)  enumList)))

(defn- findPath-aux [root matchStr accessPathList]
  (DEBUG "root= " root)
  (cond
    (vector? root)
    (findPath-vector root matchStr accessPathList)

    (map? root)
    (findPath-map root matchStr accessPathList)
    :else
    (search-leaf nil root matchStr accessPathList)))


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










(defn- replacePlaceholder [currentStr placeHolder]
  (let [;; Need to strip the {{ and }} from the placeholder before doing the lookup
        p2 (str/replace placeHolder "{{" "")
        p3 (str/replace p2 "}}" "")
        placeHolderValue (:url (get env/ENV-MAP p3))]
    (str/replace currentStr placeHolder placeHolderValue)))

;; Support the expansion of placeholder in URLs 
;; e.g "{{env1}}/branches" which will have the {{env1}} placeholders replaced
(defn- expandURL [url]
  (let [placeholderRegExp #"\{\{[^\}]*\}\}"
        placeholderList (re-seq placeholderRegExp url)
        placeholderSet (set placeholderList)]
    (reduce replacePlaceholder url placeholderSet)))

;; Extract the best response from resp
(defn- best-response [resp]
  (let [body (:body resp)
        status (:status resp)]
    (if (= body "")
      (str "SUCCESS: " status) ;; Could return resp for full response but makes it noisy
      (json/read-str body))))


;; Convert options parameters from EDN to JSON
(defn- expand-options [options]
  ;;(prn "OPTIONS: " options)
  (let [body (:body options)]
    (if (or (map? body) (vector? body))
      (assoc options :body (json/write-str body)) ; Convert body to JSON string if needed
      options)))

(use 'clojure.test)

(deftest addition2
  (is (= 3 (+ 2 2)))
  (is (= 7 (+ 3 4))))