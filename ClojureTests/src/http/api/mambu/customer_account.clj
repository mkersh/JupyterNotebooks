(ns http.api.mambu.customer_account
  (:require [http.api.json_helper :as api]
            [http.api.mambu.account :as acc]
            [http.api.mambu.customer :as cust]))

(defn close-customer-and-accounts
  [id]
  (acc/close-customer-accounts id))

(defn close-customer
  "Depending on the state of the customer delete or close the customer.
   If the customer has accounts these will have to be closed before customer 
   can be closed.
   "
  [id]

  (let
   [cust-obj (cust/get-customer id)
    status (get cust-obj "state")]
    (if (= status "INACTIVE")
      (cust/delete-customer id)
      (close-customer-and-accounts id))))

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
    (api/PRINT (api/GET url options))))
            