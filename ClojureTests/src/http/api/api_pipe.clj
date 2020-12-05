;;; Support the processing of a collection of JSON API calls
;;; 
(ns http.api.api_pipe
  (:require [http.api.json_helper :as api]
            [clojure.pprint :as pp]))

(defn save-part-to-context [part-path name]
  (fn [context _] ; 2nd param is the step object
    (let [last (:last-call context)
          part (api/get-attr last part-path)]
      (assoc context name part))))

(defn save-value-to-context [val name]
  (fn [context _] ; 2nd param is the step object
    (assoc context name val)))

(defn save-context-value-to-context [old-name new-name]
  (fn [context _] ; 2nd param is the step object
    (assoc context new-name (get context old-name))))


(defn save-last-to-context [name]
  (fn [context _] ; 2nd param is the step object
    (assoc context name (:last-call context))))

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

(defn process-api-request [context request]
  (let [request0 (request context) ; expand the request using the context
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
          new-context2)))

(defn- find-jump-pos [id-val steps-list]
  (+ 1 (count
        (take-while #(not (= (:id %1) id-val)) steps-list))))

(find-jump-pos :jump-here [1 {:id :jump-here} 3])

(defn jump-to-step [col steps-list]
  (let [jumpto (:jump-to-step col)]
    (if jumpto
      (drop (- (find-jump-pos jumpto steps-list) 1) steps-list)
      steps-list)
    ))

(defn process-collection [col]
  (reduce next-step (:context col) (jump-to-step col (:steps col)) )
  )

(comment
  (json-api-example {:custid "019327031"})

  (process-api-request (:context example-collection) json-api-example)

  (process-collection example-collection)

  (reduce prn "start" [1 2 3])
  
  )