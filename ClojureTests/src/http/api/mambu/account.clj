(ns http.api.mambu.account
  (:require [http.api.json_helper :as api]))

(defn list-loans [& opt-overrides]
  (let [detailLevel (or (first opt-overrides) "FULL")
        limitVal (or (second opt-overrides) 50)
        moreOpts (get opt-overrides 2)
        optdefs {:basic-auth (api/get-auth "env1")
                 :headers {"Accept" "application/vnd.mambu.v2+json"}
                 :query-params {"detailsLevel" (or detailLevel "FULL")
                                "paginationDetails" "ON"
                                "limit" limitVal}}
        options (merge optdefs moreOpts)
        url "{{env1}}/loans"]
    (api/PRINT (api/GET url options))))

(defn get-loan [id & opt-overrides]
  (let [detailLevel (or (first opt-overrides) "FULL")
        moreOpts (get opt-overrides 1)
        optdefs {:basic-auth (api/get-auth "env1")
                 :headers {"Accept" "application/vnd.mambu.v2+json"}
                 :query-params {"detailsLevel" detailLevel}}
        options (merge optdefs moreOpts)
        url (str "{{env1}}/loans/" id)]
    (api/PRINT (api/GET url options))))

(defn create-loan [prodid & opt-overrides]
  (let [moreOpts (first opt-overrides)
        optdefs {:basic-auth (api/get-auth "env1")
                 :headers {"Accept" "application/vnd.mambu.v2+json"
                           "Content-Type" "application/json"}
                 :query-params {}
                 :body  {"loanAmount" 30000.0
                         "loanName" "MKCurTest1"
                         "accountHolderKey" "8a8186da73ec37c20173eec481a92753"
                         "productTypeKey" prodid
                         "accountHolderType" "CLIENT"
                         "scheduleSettings" {"defaultFirstRepaymentDueDateOffset" 0
                                             "gracePeriod" 0
                                             "gracePeriodType" "NONE"
                                             "paymentPlan" []
                                             "periodicPayment" 0.0
                                             "principalRepaymentInterval" 1
                                             "repaymentInstallments" 12
                                             "repaymentPeriodCount" 1
                                             "repaymentPeriodUnit" "MONTHS"
                                             "repaymentScheduleMethod" "DYNAMIC"
                                             "scheduleDueDatesMethod" "INTERVAL"}
                         "interestSettings" {"interestRate" 2.0}}}
        options (merge optdefs moreOpts)]
    (api/PRINT (api/POST "{{env1}}/loans" options))))


(defn delete-customer [id]
  (let [options {:basic-auth (api/get-auth "env1")
                 :headers {"Accept" "application/vnd.mambu.v2+json"}
                 :query-params {}}
        url (str "{{env1}}/clients/" id)]
    (api/PRINT (api/DELETE url options))))

(defn put-customer [id & opt-overrides]
  (let [moreOpts (first opt-overrides)
        optdefs {:basic-auth (api/get-auth "env1")
                 :headers {"Accept" "application/vnd.mambu.v2+json"
                           "Content-Type" "application/json"}
                 :query-params {}
                 :body
                 {"creationDate" "2020-08-14T15:02:28+02:00"
                  "approvedDate" "2020-08-14T15:32:10+02:00"
                  "groupLoanCycle" 0
                  "preferredLanguage" "ENGLISH"
                  "lastName" "Raab2XXXXXYYYYY"
                  "id" id
                  "xid" "145566212"  ; Iterestingly you can change the id!!
                  "gender" "MALE"
                  "lastModifiedDate" "2020-08-14T12:14:13+02:00"
                  "firstName" "Jim999bbbbbbb"
                  "encodedKey" "8a81871173ec66260173ed29009d2cee"
                  "loanCycle" 0
                  "state" "INACTIVE"
                  "clientRoleKey" "8a818e74677a2e9201677ec2b4c336aa"
                  "notes" "Modified set of notes<br/>Hello world!!<br/>Another line
                                  <script>alert(1);</script>
                                  <p style= 'color:#FF0000';>Red paragraph text</p>
                                  "
                  "groupKeys" []}}
        url (str "{{env1}}/clients/" id)
        options (merge optdefs moreOpts)]
    (api/PRINT (api/PUT url options))))

(defn patch-customer [id & opt-overrides]
  (let [moreOpts (first opt-overrides)
        optdefs {:basic-auth (api/get-auth "env1")
                 :headers {"Accept" "application/vnd.mambu.v2+json"
                           "Content-Type" "application/json"}
                 :query-params {}
                 :body [{"op" "ADD"
                         "path" "firstName"
                         "value" "Jim999BBBUUUU"}
                        ;; Next OP is an example of updating a custom field
                        ;; Change the op to remove to delete
                        {"op" "add"
                         "path" "/_MKExtraCustomer/MyCustomerField1"
                         "value" "Oh yes"}
                        {"op" "ADD"
                         "path" "gender"
                         "value" "MALE"}
                        {"op" "ADD"
                         "path" "This attribute doesn't exists"
                         "value" "FEMALEff"}]}
        options (merge optdefs moreOpts)
        url (str "{{env1}}/clients/" id)]
    (api/PRINT (api/PATCH url options))))


; Test in your REPL: Select line to run ctl+alt+c <space>
; Use api/find-path and api/extract-attrs to navigate through results
(comment
  
  (time (list-loans "BASIC" 1))
  (time (list-loans))

  (def NewAccountID "SCGC121")
  (time (get-loan NewAccountID))
  (time (get-loan NewAccountID "BASIC"))

  (time (create-loan "8a8187366a01d4a1016a023792a500b9"))
  (time (create-loan {:body  {"firstName" "Charles"
                                  "lastName" "Brown"}}))

  (time (delete-customer NewCustomerID))

  (time (patch-customer NewCustomerID {:body [{"op" "ADD"
                                               "path" "firstName"
                                               "value" "1212121212121212"}]}))
  (time (patch-customer NewCustomerID))

  (time (put-customer "580959603" {:body {"creationDate" "2020-08-14T22:24:47+02:00"
                                          "idDocuments" []
                                          "groupLoanCycle" 0
                                          "preferredLanguage" "ENGLISH"
                                          "lastName" "Brownxx"
                                          "id" "580959603"
                                          "lastModifiedDate" "2020-08-14T22:24:47+02:00"
                                          "firstName" "Charles"
                                          "encodedKey" "8a81871173ec66260173eea5761e1d9e"
                                          "addresses" []
                                          "loanCycle" 0
                                          "state" "INACTIVE"
                                          "clientRoleKey" "8a818e74677a2e9201677ec2b4c336aa"}}))
  (time (put-customer NewCustomerID)))


(use 'clojure.test)

(deftest addition
  (is (= 4 (+ 2 2)))
  (is (= 7 (+ 3 4))))

(comment
(run-tests 'http.api.account)

(run-all-tests #"http.*")
)