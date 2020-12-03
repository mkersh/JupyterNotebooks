(ns http.api.mambu.demo.credit_card_pipe
  (:require [http.api.json_helper :as api]
             [http.api.api_pipe :as steps]))


(def create-customer
  (fn [context]
    {:url (str "{{*env*}}/clients/" (:custid context))
     :method api/POST
     :headers {"Accept" "application/vnd.mambu.v2+json"
               "Content-Type" "application/json"}
     :query-params {}
     :body {"firstName" (:first-name context)
            "lastName" (:last-name context)
            "preferredLanguage" "ENGLISH"
            "addresses" [{"country" "UK"
                          "city" "Liverpool"}]
            "notes" "Some Notes on this person"
            "gender" "MALE"
            "assignedBranchKey" (:branchid context)}
     }))

(def delete-customer
  (fn [context]
    {:url (str "{{*env*}}/clients/" (:custid context))
     :method api/DELETE
     :headers {"Accept" "application/vnd.mambu.v2+json"
               "Content-Type" "application/json"}
     }))

;; A collection of steps     
(def create-cc-collection
  {:context {:first-name "John", :last-name "Barry4"}
   :steps [{:request create-customer
            :post-filter [(steps/save-part-to-context ["id"] :custid)
                          ;;(steps/save-part-to-context [0 "birthDate"] :last-date)
                          ;;(save-last-to-context :cust-list)
                          ]}
            {:request delete-customer }

        ;;    {
        ;;     :request (fn [_] 
        ;;                {:method (fn [_,_] (prn "Step 2"))})
        ;;    }
        ]}
           
           )

(comment
  (api/setenv "env2")
  (steps/process-collection create-cc-collection)
  
  )