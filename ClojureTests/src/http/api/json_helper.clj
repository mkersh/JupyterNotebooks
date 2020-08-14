;; Helper functions to calling JSON APIs using http://http-kit.github.io/
;; GitHub: https://github.com/mkersh/JupyterNotebooks/blob/master/ClojureTests/src/http/api/json_helper.clj 
(ns http.api.json_helper
  (:require
   [clojure.data.json :as json]
   [org.httpkit.client :as client]
   [clojure.string :as str]
   [http.ENV :as env] ; This is the file to store env secrets in - make sure its in your .gitignore
   [clojure.pprint :as pp]))

(declare expandURL expand-options best-response)

;;; ----------------------------------------------------------------------
;;; JSON - Helper Methods:
;;; 
;;; GET, POST, DELETE, PATCH, PUT
;;; 

(defn- request [url, method, options0]
  (let [url-expanded (expandURL url)
        options (expand-options options0)
        response @(method url-expanded options)
        status (:status response)]

    (if (< status 300)
      (prn "Successful Call: " status)
      (prn "ERROR Status: " status))
    (best-response response)))

(defn- GET2 [url, options]
  (request url, client/get, options)
)

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

;;; ----------------------------------------------------------------------
;;; Print and Extraction helper functions
;;; 
;;; 

(defn PRINT [http-kit-response]
  (let [num-items (count http-kit-response)]
    (pp/pprint http-kit-response) ; This was pretty-printing the complete result
    (prn "Number of items:" num-items)
    ;(map #(get % attr) result) ; This is now just selecting the top level "id" keys from the results
                               ; Need to make this more readable though by introducing a higher level function 
    ))


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

;; Extract the best response from resp
(defn best-response [resp]
  (let [body (:body resp)
        status (:status resp)]
    (if (= body "")
      (str "SUCCESS: " status) ;; Could return resp for full response but makes it noisy
      (json/read-str body))))


;; Convert options parameters from EDN to JSON
(defn expand-options [options]
  (prn "OPTIONS: " options)
  (let [body (:body options)]
    (if (or (map? body) (vector? body))
      (assoc options :body (json/write-str body)) ; Convert body to JSON string if needed
      options)))

