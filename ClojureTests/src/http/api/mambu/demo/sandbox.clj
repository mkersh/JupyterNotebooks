(ns http.api.mambu.demo.sandbox
  (:require [http.api.json_helper :as api]
            [http.api.api_pipe :as steps]))


(defn get-deposit-accounts-api [context]
  {:url (str "{{*env*}}/deposits/")
   :method api/GET
    :query-params {
        "detailsLevel" "FULL"
        "accountHolderType" "CLIENT"
        "accountHolderId" (:custid context)
        }
   :headers {"Accept" "application/vnd.mambu.v2+json"
             "Content-Type" "application/json"}})

(defn get-loan-accounts-api [context]
  {:url (str "{{*env*}}/loans/")
   :method api/GET
   :query-params {"detailsLevel" "FULL"
                  "accountHolderType" "CLIENT"
                  "accountHolderId" (:custid context)}
   :headers {"Accept" "application/vnd.mambu.v2+json"
             "Content-Type" "application/json"}})


(defn match-productkey [context accListID]
(let [accList (accListID context)
      productKey (:productKey context)]
  (filter #(= (get % "productTypeKey") productKey) accList))
)
(defn find-customer-account [context]
  (let [context1 (steps/apply-api get-deposit-accounts-api context :trans_details)
        resList (match-productkey context1 :trans_details)]
    (if (> (count resList) 0)
      (first resList)
      ;; No deposit account match look in loan accounts
      (let [context1 (steps/apply-api get-loan-accounts-api context :trans_details)
            resList2 (match-productkey context1 :trans_details)]
        (if (> (count resList2) 0)
          (first resList2)
          nil)))))

(comment
  (api/setenv "env2")
  ;; 8a818f5f6cbe6621016cbf6d66075e54 - RCA purchase PROD ID
  ;; 8a818f5f6cbe6621016cbf7310ff6064 - TEMP Card PROD ID
  (def context {:custid "267931320" :productKey "8a818f5f6cbe6621016cbf6d66075e54"})
  (find-customer-account context)

  (count [])
)