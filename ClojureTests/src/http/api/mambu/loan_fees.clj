(ns http.api.mambu.loan-account
  (:require [http.api.json_helper :as api]
            [http.api.mambu.loan-account :as loan]
            [http.api.mambu.product-type :as pt]
            ))

(defn apply-fee [loanid feeid amount installNumber]
  (let [options {:basic-auth (api/get-auth)
                 :headers {"Accept" "application/vnd.mambu.v2+json"
                           "Content-Type" "application/json"}
                 :query-params {}
                 :body {"amount" amount
                        "predefinedFeeKey" feeid
                        "installmentNumber" installNumber}}
        url (str "{{env1}}/loans/" loanid "/fee-transactions")]
    (api/PRINT (api/POST url options))))

(defn enumerate [s]
  (map #(vector %2 %1) s (iterate inc 1)))

(defn apply-VAT [loanid feeid vatPercentage [instalNum _]]
  (apply-fee loanid feeid vatPercentage instalNum)
  )

(defn apply-VAT-to-instalments
  [loanid feeid vatPercentage]
  (let [schedule-list (enumerate (get (loan/loan-schedule loanid {:no-print true}) "installments"))]
    (map #(apply-VAT loanid feeid vatPercentage %1)  schedule-list)))


(comment

(api/setenv "env2")
(def NewAccountID "QRAL117")
(time (loan/get-loan NewAccountID))
(time (loan/loan-schedule NewAccountID))
(def feeID "8a818ece75a3211a0175a84cd03d1307")
(time (apply-fee NewAccountID feeID 11 36))
(time (apply-VAT-to-instalments NewAccountID feeID 23.0))
(loan/loan-schedule NewAccountID {:no-print true})

;; find the productTypeKey on the loan account
(time (pt/get-loan-product "8a818ece75a3211a0175a83127834f39"))

)