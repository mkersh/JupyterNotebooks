(ns http.api.customer
  (:require [http.api.json_helper :as api]))

(defn list-customers [& opt-overrides ]
  (let [detailLevel (or (first opt-overrides) "FULL")
        limitVal (or (second opt-overrides) 50)
        moreOpts (get opt-overrides 2)
        optdefs {:basic-auth (api/get-auth "env1")
                 :headers {"Accept" "application/vnd.mambu.v2+json"}
                 :query-params {
                                "detailsLevel" (or detailLevel "FULL")
                                "paginationDetails" "ON"
                                 "limit" limitVal}}
        options (merge optdefs moreOpts)
        url "{{env1}}/clients"]
    (api/PRINT (api/GET url options))))

(defn get-customer [id]
  (let [options {:basic-auth (api/get-auth "env1")
                 :headers {"Accept" "application/vnd.mambu.v2+json"}
                 :query-params {"detailsLevel" "BASIC"}}
        url (str "{{env1}}/clients/" id)]
    (api/PRINT (api/GET url options))))

(defn create-customer []
  (let [options {:basic-auth (api/get-auth "env1")
                 :headers {"Accept" "application/vnd.mambu.v2+json"
                           "Content-Type" "application/json"}
                 :query-params {}
                 :body  {"firstName" "Dominic"
                         "lastName" "Raab2"
                         "preferredLanguage" "ENGLISH"
                         "addresses" [{"country" "UK"
                                       "city" "Liverpool"}]
                         "notes" "Some Notes on this person"
                         "gender" "MALE"
                         "identificationDocumentTemplateKey" "8a81879867f40eff0167f45206e8002b"}}]
    (api/PRINT (api/POST "{{env1}}/clients" options))))


(defn delete-customer [id]
  (let [options {:basic-auth (api/get-auth "env1")
                 :headers {"Accept" "application/vnd.mambu.v2+json"}
                 :query-params {}}
        url (str "{{env1}}/clients/" id)]
    (api/PRINT (api/DELETE url options))))

(defn put-customer [id]
  (let [options {:basic-auth (api/get-auth "env1")
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
        url (str "{{env1}}/clients/" id)]
    (api/PRINT (api/PUT url options))))

(defn patch-customer [id]
  (let [options {:basic-auth (api/get-auth "env1")
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
        url (str "{{env1}}/clients/" id)]
    (api/PRINT (api/PATCH url options))))


; Test in your REPL: Select line to run ctl+alt+c <space>
; Use api/find-path and api/extract-attrs to navigate through results
(comment 
  (def NewCustomerID "917544150")
  (time (list-customers "FULL" nil {:query-params {"detailsLevel" "BASIC"}}))
  (time (list-customers "BASIC"))
  (time (list-customers "FULL" 5))
  (time (list-customers))
  
  (time (get-customer NewCustomerID))
  (time (create-customer))
  (time (delete-customer NewCustomerID))
  (time (patch-customer NewCustomerID))
  (time (put-customer NewCustomerID))
  
  (merge {:f 1 :g 2} {:f 3})
  )


(def list1 [1])

(first list1)

(second list1)

(get list1 2)