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

(comment
  (api/setenv "env2")

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
                 :trans_id "8a818f27768928fb01768a4667f627e2"})

  (steps/apply-api add-card-trans-api card-obj :card_trans)
  (steps/apply-api get-transaction-api card-obj :trans_details)
  (steps/apply-api get-all-trans-api card-obj :trans_details)

  ;;(steps/apply-api revert-transaction-api card-obj :trans_details)
  (steps/apply-api reverse-card-trans-api card-obj :trans_details)

  (steps/apply-api list-cards-api card-obj :card_list)
  (steps/apply-api link-card-api card-obj :link_card)
  
  )
