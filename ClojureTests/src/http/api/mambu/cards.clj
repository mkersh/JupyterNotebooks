(ns http.api.mambu.cards
  (:require [http.api.json_helper :as api]))

(defn list-cards [accid]
  (let [options {:basic-auth (api/get-auth "env1")
                 :headers {"Accept" "application/vnd.mambu.v2+json"}
                 :query-params {"detailsLevel" "FULL"}}
        url (str "{{env1}}/deposits/" accid "/cards")]
    (api/PRINT (api/GET url options))))


(defn link-card [accid card-token]
  (let [options {:basic-auth (api/get-auth "env1")
                 :headers {"Accept" "application/vnd.mambu.v2+json"
                           "Content-Type" "application/json"}
                 :query-params {}
                 :body {"referenceToken" card-token}}
        url (str "{{env1}}/deposits/" accid "/cards")]
    (api/PRINT (api/POST url options))))

(defn unlink-card [accid card-token]
  (let [options {:basic-auth (api/get-auth "env1")
                 :headers {"Accept" "application/vnd.mambu.v2+json"
                           "Content-Type" "application/json"}
                 :query-params {}}
        url (str "{{env1}}/deposits/" accid "/cards/" card-token)]
    (api/PRINT (api/DELETE url options))))


(defn create-hold [card-token amount transRef]
  (let [options {:basic-auth (api/get-auth "env1")
                 :headers {"Accept" "application/vnd.mambu.v2+json"
                           "Content-Type" "application/json"}
                 :query-params {}
                 :body {"externalReferenceId" transRef
                        "amount" amount
                        "advice" false
                        "cardAcceptor" {"zip" "zipCode"
                                        "country" "CountryStr"
                                        "city" "CityStr"
                                        "name" "Merchant Name"
                                        "state" "State"
                                        "mcc" 77}
                        "userTransactionTime" "11:10:15"
                        "currencyCode" "EUR"}}
        url (str "{{env1}}/cards/" card-token "/authorizationholds")]
    (api/PRINT (api/POST url options))))

(defn increase-hold [card-token amount transRef]
  (let [options {:basic-auth (api/get-auth "env1")
                 :headers {"Accept" "application/vnd.mambu.v2+json"
                           "Content-Type" "application/json"}
                 :query-params {}
                 :body {"amount" amount}}
        url (str "{{env1}}/cards/" card-token "/authorizationholds/" transRef ":increase")]
    (api/PRINT (api/POST url options))))

(defn decrease-hold [card-token amount transRef]
  (let [options {:basic-auth (api/get-auth "env1")
                 :headers {"Accept" "application/vnd.mambu.v2+json"
                           "Content-Type" "application/json"}
                 :query-params {}
                 :body {"amount" amount}}
        url (str "{{env1}}/cards/" card-token "/authorizationholds/" transRef ":decrease")]
    (api/PRINT (api/POST url options))))


(defn list-holds [accid & opt-overrides]
  (let [moreOpts (first opt-overrides)
        status (:status moreOpts) 
        options {:basic-auth (api/get-auth "env1")
                 :headers {"Accept" "application/vnd.mambu.v2+json"}
                 :query-params {"detailsLevel" "FULL"
                                "status" status}}
        url (str "{{env1}}/deposits/" accid "/authorizationholds")]
    (api/PRINT (api/GET url options))))

(defn delete-hold [card-token transRef]
  (let [options {:basic-auth (api/get-auth "env1")
                 :headers {"Accept" "application/vnd.mambu.v2+json"
                           "Content-Type" "application/json"}
                 :query-params {}}
        url (str "{{env1}}/cards/" card-token "/authorizationholds/" transRef)]
    (api/PRINT (api/DELETE url options))))

(defn get-hold [card-token transRef & opt-overrides]
  (let [moreOpts (first opt-overrides)
        options {:basic-auth (api/get-auth "env1")
                 :headers {"Accept" "application/vnd.mambu.v2+json"
                           "Content-Type" "application/json"}
                 :query-params {}}
        url (str "{{env1}}/cards/" card-token "/authorizationholds/" transRef)]
    (api/PRINT (api/GET url options) moreOpts)))

(defn get-num [strNum]
  (BigDecimal. strNum))

(defn zero-hold [card-token transRef]
  (let [amount (get-num (get (get-hold card-token transRef {:no-print true}) "amount"))
        near-zero (- amount 0.0000000001)]
        (decrease-hold card-token near-zero transRef)))

(defn settle-transaction [card-token amount transRef]
  (let [options {:basic-auth (api/get-auth "env1")
                 :headers {"Accept" "application/vnd.mambu.v2+json"
                           "Content-Type" "application/json"}
                 :query-params {}
                 :body {"amount" amount
                        "advice" false
                        "externalAuthorizationReferenceId" transRef
                        "externalReferenceId" transRef
                        "transactionChannelId" "8a818e74677a2e9201677ec2b4c336a6"
                        }}
        url (str "{{env1}}/cards/" card-token "/financialtransactions")]
    (api/PRINT (api/POST url options))))

(defn create-transaction [card-token amount transRef]
  (let [tref (or transRef (api/uuid))
        options {:basic-auth (api/get-auth "env1")
                 :headers {"Accept" "application/vnd.mambu.v2+json"
                           "Content-Type" "application/json"}
                 :query-params {}
                 :body {"amount" amount
                        "advice" false
                        "notes" "Note associated with the transaction"
                        "externalReferenceId" tref
                        "transactionChannelId" "8a818e74677a2e9201677ec2b4c336a6"}}
        url (str "{{env1}}/cards/" card-token "/financialtransactions")]
    (api/PRINT (api/POST url options))))

; Test in your REPL: Select line to run ctl+alt+c <space>
; Use api/find-path and api/extract-attrs to navigate through results
(comment
  (time (list-cards "BUKO329"))
  (time (link-card "BUKO329" "token1"))
  (time (unlink-card "BUKO329" "token1"))


  (time (create-hold "token1" 12.33 "0711_1"))
  (time (list-holds "BUKO329"))
  (time (list-holds "BUKO329" {:status "PENDING"}))
  (time (delete-hold "token1" "0711_1"))
  (time (get-hold "token1" "0711_1"))

  (def transRef (api/uuid))
  (time (create-hold "token1" 100 transRef))
  (time (increase-hold "token1" 200.00 transRef))
  (time (decrease-hold "token1" 100.00 transRef))
  (time (zero-hold "token1" transRef))
  (time (get-hold "token1" transRef))

  (time (settle-transaction "token1" 99.13 transRef))

  ;; Create a realtime card financial transactions i.e no previous hold to match
  (time (create-transaction "token1" 377 nil))


  (- 100 0.00001)
  (def hold1 {"advice" false
              "cardToken" "token1"
              "creditDebitIndicator" "DBIT"
              "cardAcceptor"
              {"mcc" 77
               "name" "Merchant Name"
               "city" "CityStr"
               "state" "State"
               "zip" "zipCode"
               "country" "CountryStr"}
              "status" "PENDING"
              "externalReferenceId" "0711_4"
              "currencyCode" "EUR"
              "encodedKey" "8a8186fc75a2bb460175a2f330960069"
              "userTransactionTime" "11:10:15"
              "amount" 100.0})

  (get  hold1 "amount"))