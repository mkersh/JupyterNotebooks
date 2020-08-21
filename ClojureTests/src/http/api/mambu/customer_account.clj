(ns http.api.mambu.customer_account
  (:require [http.api.json_helper :as api]
            [http.api.mambu.account :as acc]
            [http.api.mambu.customer :as cust]))

(defn get-customer-loans [id & opt-overrides]
  (let [encID     (cust/get-customer-encid id)
        detailLevel (or (first opt-overrides) "FULL")
        limitVal (or (second opt-overrides) 50)
        moreOpts (get (into [] opt-overrides) 2)
        optdefs {:basic-auth (api/get-auth "env1")
                 :headers {"Accept" "application/vnd.mambu.v2+json"}
                 :query-params {"detailsLevel" (or detailLevel "FULL")
                                "paginationDetails" "ON"
                                "limit" limitVal
                                "accountHolderType" "CLIENT"
                                "accountHolderId" encID}}
        options (merge optdefs moreOpts)
        url "{{env1}}/loans"]
    (prn "*******get-customer-loans " options )
    (api/PRINT (api/GET url options) options)))

(defn close-customer-and-accounts [id]
  (prn "close-customer-accounts")
  (map acc/close-account (get-customer-loans id "BASIC" 999 {:no-print true})))


(defn close-customer
   "Depending on the state of the customer delete or close the customer.
   If the customer has accounts these will have to be closed before customer 
   can be closed."
   [id]

   (let
    [cust-obj (cust/get-customer id "BASIC" {:no-print true})
     status (get cust-obj "state")]
     (if (= status "INACTIVE")
       (try
         (cust/delete-customer id {:throw-errors true}) ; NOTE: This still may fail because customer may have inactive accounts
         (catch Exception _ (close-customer-and-accounts id)))

       (close-customer-and-accounts id))))

(comment
  
  (time (close-customer "756828242"))
  (cust/get-customer "756828242")
  
  (time (get-customer-loans "756828242"))
  (time (get-customer-loans "8a8186da73ec37c20173eec481a92753"))
  (def custObj {"creationDate" "2020-08-14T22:58:42+02:00"
              "approvedDate" "2020-08-14T22:58:42+02:00"
              "groupLoanCycle" 0
              "preferredLanguage" "ENGLISH"
              "lastName" "Brown"
              "id" "756828242"
              "lastModifiedDate" "2020-08-14T22:58:42+02:00"
              "firstName" "Charles"
              "encodedKey" "8a8186da73ec37c20173eec481a92753"
              "loanCycle" 0
              "state" "INACTIVE"
              "clientRoleKey" "8a818e74677a2e9201677ec2b4c336aa"})
  (time (get-customer-loans custObj))
  
  
  )