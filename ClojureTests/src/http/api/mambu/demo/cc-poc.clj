(ns http.api.mambu.demo.cc-poc
  (:require [http.api.json_helper :as api]
            [http.api.api_pipe :as steps]))


(defn list-cards-api [context]
  {:url (str "{{*env*}}/deposits/" (:accid context) "/cards")
   :method api/GET
   :query-params {"detailsLevel" "FULL"}
   :headers {"Accept" "application/vnd.mambu.v2+json"
             "Content-Type" "application/json"}})

(defn link-card-api [context]
  {:url (str "{{*env*}}/deposits/" (:accid context) "/cards")
   :method api/POST
   :body   {"referenceToken" (:card-token context)}
   :headers {"Accept" "application/vnd.mambu.v2+json"
             "Content-Type" "application/json"}})

(defn add-card-trans-api [context]
  {:url (str "{{*env*}}/cards/" (:card-token context) "/financialtransactions")
   :method api/POST
   :body   {"amount" (:amount context)
            "advice" false
            "cardAcceptor" (:card-acceptor context)
            "externalReferenceId" (:trans_ref context)
            "transactionChannelId" (:channelid context)}
   :headers {"Accept" "application/vnd.mambu.v2+json"
             "Content-Type" "application/json"}})

(defn get-all-trans-api [context]
  {:url (str "{{*env*}}/deposits/" (:accid context) "/transactions")
   :method api/GET
   :query-params {"detailsLevel" "FULL"}
   :headers {"Accept" "application/vnd.mambu.v2+json"
             "Content-Type" "application/json"}})

(defn get-transaction-api [context]
  {:url (str "{{*env*}}/deposits/transactions:search")
   :method api/POST
   :query-params {"detailsLevel" "FULL"}
   :body {"filterCriteria" [{"field" "encodedKey"
                             "operator" "EQUALS"
                             "value" (:trans_id context)}]}
   :headers {"Accept" "application/vnd.mambu.v2+json"
             "Content-Type" "application/json"}})

;; Next doesn't work for card transactions - use reverse-card-trans-api instead
(defn revert-transaction-api [context]
  {:url (str "{{*env*}}/deposits/transactions/" (:trans_id context) ":adjust")
   :method api/POST
   :body {"notes" "Revert cards transaction after move"}
   :headers {"Accept" "application/vnd.mambu.v2+json"
             "Content-Type" "application/json"}})

(defn reverse-card-trans-api [context]
  {:url (str "{{*env*}}/cards/" (:card-token context) "/financialtransactions/" (:trans_ref context) ":decrease")
   :method api/POST
   :body {"amount" (:amount context)
          "externalReferenceId" (:trans_ref context)}
   :headers {"Accept" "application/vnd.mambu.v2+json"
             "Content-Type" "application/json"}})


(defn create-installment-loan-api [context]
  {:url (str "{{*env*}}/loans")
   :method api/POST
   :headers {"Accept" "application/vnd.mambu.v2+json"
             "Content-Type" "application/json"}
   :query-params {}
   :body  {"loanAmount" (:amount context)
           "loanName" (:acc-name context)
           "accountHolderKey" (:cust-key context)
           "productTypeKey" (:prod-key context)
           "accountHolderType" "CLIENT"
           "assignedBranchKey" (:branchid context)
           "interestFromArrearsAccrued" 0.0
           "interestSettings" {"interestSpread" (:interestspread context)}
           "scheduleSettings" {"fixedDaysOfMonth" [(:payment-day context)]
                               "gracePeriod" (:grace_period context)
                               "repaymentInstallments" (:num-installments context)}}})


(defn search-for-loan-api [context]
  {:url (str "{{*env*}}/loans:search")
   :method api/POST
   :query-params {"detailsLevel" "FULL"}
   :body {"filterCriteria" [{"field" "accountHolderKey"
                             "operator" "EQUALS"
                             "value" (:cust-key context)}
                            {"field" "productTypeKey"
                             "operator" "EQUALS"
                             "value" (:prod-key context)}]}
   :headers {"Accept" "application/vnd.mambu.v2+json"
             "Content-Type" "application/json"}})


(defn disburse-rca-api [context]
  {:url (str "{{*env*}}/loans/" (:accid context) "/disbursement-transactions")
   :method api/POST
   :body {"amount" (:amount context)
          "bookingDate" (:bookdate context)
          ;; There is some risk with trying to set the valueDate?? Leaving off for the MVP
          ;; "valueDate" (:bookdate context)
          "transactionDetails" {"transactionChannelKey" (:move_channel context)}
          "_credit-card-fields"
          {"cardAcceptor" (:card_acceptor context)}}
   :headers {"Accept" "application/vnd.mambu.v2+json"
             "Content-Type" "application/json"}})

(defn add-context-parameter [context nested-fields name]
  (let [val (api/get-attr context nested-fields)]
  (assoc context name val)
  ))

(defn add-context-parameter-filter [context nested-fields name filterFn]
  (let [val (api/get-attr context nested-fields)]
    (assoc context name (filterFn val))))

(defn move-cardtrans-to-rca [context]
;; Use card-acceptor details in :trans_details to determine whether to allocate to cash or purchases RCA bucket
;; TEMP: Will initially just allocate to purchases 
(let [rca_acc  (steps/apply-api search-for-loan-api context :loan_search)
      card_tran (steps/apply-api get-transaction-api rca_acc :trans_details)
      context3 (add-context-parameter card_tran [:loan_search 0 "id"] :accid)
      context4 (add-context-parameter context3 [:trans_details 0 "valueDate"] :bookdate)
      context5 (add-context-parameter-filter context4 [:trans_details 0 "amount"] :amount #(- %)) ;; card trans will be negative, change to positive
      context6 (add-context-parameter-filter context5 [:trans_details 0 "cardTransaction" "cardAcceptor"] :card_acceptor api/to-json)
      t1 (prn "*****BEFORE Disburse*** ")
      disburse  (steps/apply-api disburse-rca-api context6 :rca_disburse)]
  disburse)
)

(defn move-cardtrans-to-ind-loan [context])

(defn move-cardtrans-to-loan [context]
;; 1) - Get card details
;; 2) - Create new loan for transaction
;; 2.1) - Copy in the cardAcceptor as custom fields
;; 3) - reverse the original transaction
;; 3.1) - Need to be careful here to be able to differentiate these internal reversals from actual chargebacks
;; - Might be best to just deposit money back in rather than reverse
;; 
  (let [card_trans (steps/apply-api get-transaction-api context :trans_details)
        alloc_type (:loan_type context)]

;; TODO - After moving then reverse
    (if (= alloc_type "RCA")
      (move-cardtrans-to-rca card_trans)
      (move-cardtrans-to-ind-loan card_trans))))

(comment
  (api/setenv "env2")

  (def move-obj {:move_channel "8a818fc1768a3af801768b8020d14a63"
                 :cust-key "8a818f857671599e0176755250d61cea"
                 :trans_id "8a818ff47688c51401768a43d42e280a" ; Card transaction to move
                 :loan_type "RCA" ; RCA | BNPL | INSTAL | PROM
                 :prod-key "8a818f5f6cbe6621016cbf3cf8675424" ; DEBUG only
                 :rca_cash_product "8a818f5f6cbe6621016cbf3cf8675424"
                 :rca_shop_product "8a818f5f6cbe6621016cbf6d66075e54"
                 :install_product {:prod-key "8a818f657688e84c017689f6a76a2f95" :acc-name "Install Loan"}
                 :bnpl_product {:prod-key "8a818f657688e84c01768ad0da5c54dc" :acc-name "BNPL Loan"}
                 :prom_product {:prod-key "8a818f657688e84c01768ade5b6361c2" :acc-name "Promo Loan"}})
  
  (move-cardtrans-to-rca move-obj)

  (add-context-parameter  move-obj
                          [:rca_shop_product] :prod-key)

  (move-cardtrans-to-loan move-obj)
  (steps/apply-api search-for-loan-api move-obj :loan_search)


  (def loan-obj {:cust-key "8a818f857671599e0176755250d61cea"
                 :xacc-name "Installment Loan XXX"
                 :yacc-name "BNPL"
                 :acc-name "Prom Loan"
                 :amount 43.21
                 :branchid nil
                 :interestspread 0.0
                 :payment-day 1
                 :num-installments 24
                 :grace_period 12
                 :xprod-key "8a818f657688e84c017689f6a76a2f95" ; Installment loan
                 :yprod-key "8a818f657688e84c01768ad0da5c54dc" ; BNPL loan
                 :prod-key "8a818f657688e84c01768ade5b6361c2" ; Promotional loan - with 0% grace period
                 })
  (steps/apply-api create-installment-loan-api loan-obj :new_loan)

  (def card-acceptor
    {"city" "London"
     "country" "United Kingdom"
      ;; https://www.web-payment-software.com/online-merchant-accounts/mcc-codes/
     "mcc" 5655
     "name" "Sports Direct"
     "state" "London"
     "street" "Oxford Street"
     "zip" "S10 7EL"})

  (def card-obj {:accid "VFUI798"
                 :card-token "cardtoken_1222_1"
                 :amount 22
                 :card-acceptor card-acceptor
                 :channelid "8a818e74677a2e9201677ec2b4c336a6"
                 :trans_ref "87af5ad7-f238-49af-81fe-14a6b23832ed" ; (api/uuid)
                 :trans_id "8a818ff47688c51401768a43d42e280a"})

  (steps/apply-api add-card-trans-api card-obj :card_trans)
  (steps/apply-api get-transaction-api card-obj :trans_details)
  (steps/apply-api get-all-trans-api card-obj :trans_details)

  ;;(steps/apply-api revert-transaction-api card-obj :trans_details)
  (steps/apply-api reverse-card-trans-api card-obj :trans_details)

  (steps/apply-api list-cards-api card-obj :card_list)
  (steps/apply-api link-card-api card-obj :link_card))
