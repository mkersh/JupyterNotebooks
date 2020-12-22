;;; Support the processing of a collection of JSON API calls
;;; 
(ns http.api.api_pipe
  (:require [http.api.json_helper :as api]
            [clojure.pprint :as pp]))

(defn show-only-val [context, nm, val]
  (let [show-only? (:show-only context)]
    (if show-only?  (str "{{" (name nm) "}}")
        val)))

(defn save-part-to-context [part-path name]
  (fn [context _] ; 2nd param is the step object
    (let [last (:last-call context)
          part (api/get-attr last (show-only-val context name part-path))]
      (assoc context name (show-only-val context name part)))))

(defn save-value-to-context [val name]
  (fn [context _] ; 2nd param is the step object
    (assoc context name (show-only-val context name val))))

(defn save-context-value-to-context [old-name new-name]
  (fn [context _] ; 2nd param is the step object
    (assoc context new-name (show-only-val context name (get context old-name)))))


(defn save-last-to-context [name]
  (fn [context _] ; 2nd param is the step object
    (assoc context name (:last-call context))))

(defn append-last-to-context [item-id name]
  (fn [context _] ; 2nd param is the step object
    (let [previous-val (get context name)]
      (assoc context name (conj previous-val {item-id (:last-call context)})))))

;; merge with save-last-to-context when have time
(defn save-last-to-context2 [item-id name]
  (fn [context _] ; 2nd param is the step object
      (assoc context name {item-id (:last-call context)})))


(defn print-context [label]
  (fn [context _] ; 2nd param is the step object
    (do (prn label)
        (pp/pprint context))
    context))
      

;;; *******************************************************
;;; Example datastructures that this file handles

;; An individual API request
;; Part of a parent collection See example-collection below
(def json-api-example
  ;; Each request has to be wrapped in a fn (to delay evaluation until we are ready)
  ;; This is the only tricky part of the setup
  ;; Reason: We need to delay the evaluation of the map that defines the request
  ;; until the context from the previous step has been evaluated  
  (fn [context]
    {:url (str "{{*env*}}/clients/" (:custid context))
     :method api/GET
     :headers {"Accept" "application/vnd.mambu.v2+json"}
     :query-params {}}))

;; A collection of steps     
(def example-collection 
{
    :context {:accID 1, :custName "Smith"}
    :steps [{:pre-filter nil
             :request json-api-example
             ;;:post-filter (save-last-to-context :cust-list)}
             :post-filter [(save-part-to-context [0 "id"] :last-id)
                           (save-part-to-context [0 "birthDate"] :last-date)
                           ;(save-last-to-context :cust-list)
                           ]}
             
            {:pre-filter nil
             :request (fn [_] ; again remember to wrap requests as functions
                        {:method (fn [_,_] (prn "Step 2"))})
             :post-filter nil}
            {:request (fn [_]
                        {:method (fn [_,_] (prn "Step 3"))})}]
}
)

(defn method-name [fn]
  (cond
    (= fn api/GET) "GET"
    (= fn api/POST) "POST"
    (= fn api/DELETE) "DELETE"
    (= fn api/PUT) "PUT"
    (= fn api/PATCH) "PATCH"
    :else "UNKNOWN-METHOD"))

(defn show-only-method [context req]
  (let [show-only (:show-only context)]
    (if show-only
      (assoc req :method (method-name (:method req)))
      req)))

(defn process-api-request [context request]
  (let [request0 (show-only-method context (request context)) ; expand the request using the context
        url (:url request0)
        api-method (:method request0)]
    (if (:show-only context)
      (do (prn "DEBUG:")
          (pp/pprint request0))
      (api-method url request0))    
    ))

(defn process-filter [filterFn context step]
  (if (vector? filterFn)
    (reduce #(process-filter %2 %1 step) context filterFn)
    (filterFn context step)))

(defn next-step [context step]
  (if (:ignore-rest context) context
      (let [pre-filter (:pre-filter step)
            new-context1 (if pre-filter
                           (process-filter pre-filter context step)
                           context)
            request (:request step)
            request-results (if request (process-api-request new-context1 request) nil)
            new-context2 (assoc new-context1 :last-call request-results)
            post-filter (:post-filter step)]
        (if post-filter
          (process-filter post-filter new-context2 step)
          new-context2))))

(defn- find-jump-pos [id-val steps-list]
  (+ 1 (count
        (take-while #(not (= (:id %1) id-val)) steps-list))))

(find-jump-pos :jump-here [1 {:id :jump-here} 3])

(defn jump-to-step [col steps-list]
  (let [jumpto (:jump-to-step col)
        jumpId (if (vector? jumpto)
                 (first jumpto)
                 jumpto)
        oneOnly (if (vector? jumpto)
                  (= (second jumpto) :one-only)
                  false)
        ]
    (if jumpto
      (let [start-from (drop (- (find-jump-pos jumpId steps-list) 1) steps-list)]
        (if oneOnly (take 1 start-from) start-from))
      steps-list)
    ))

;; Test jump-to-step
;;(jump-to-step {:jump-to-step [:jump-here :one-only]} [1 2 {:id :jump-here} 3 4 5])

(defn process-collection [col]
  (reduce next-step (:context col) (jump-to-step col (:steps col)) )
  )

;; Next function is an easy way to call individual API calls
(defn apply-api [api-obj context save-results-name]
  (let [steps {:context context
               :steps [{:request api-obj
                        :post-filter (save-last-to-context save-results-name)}]}
        context2 (process-collection steps)]
    context2))

(comment
  (json-api-example {:custid "019327031"})

  (process-api-request (:context example-collection) json-api-example)

  (process-collection example-collection)

  (reduce prn "start" [1 2 3])
  
  )