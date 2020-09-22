(ns http.api.mambu.customer
  (:require [http.api.json_helper :as api]))

 (defn list-views-v1 [& opt-overrides]
   (let [moreOpts (first opt-overrides)
         optdefs {:basic-auth (api/get-auth "env1")
                  :headers {}
                  :query-params {}}
         options (merge optdefs moreOpts)
         url "{{env1}}/users/apiUser/views"]
     (prn options)
     (api/PRINT (api/GET url options)))) 

;; For valid values of type see: https://support.mambu.com/docs/en/custom-views-api
(defn get-views-v1 [type viewfilter & opt-overrides]
  (let [moreOpts (first opt-overrides)
        detailLevel (or (:details-level moreOpts) "FULL_DETAILS")
        limit (or (:limit moreOpts) 2)
        offset (or (:offset moreOpts) 0)
        optdefs {:basic-auth (api/get-auth "env1")
                 :headers {}
                 :query-params {"detailsLevel" detailLevel, "limit" limit, "offset" offset, "viewfilter" viewfilter}}
        options (merge optdefs moreOpts)
        url (str "{{env1}}/" type)]
    (prn options)
    (api/PRINT (api/GET url options))))

(comment
  (list-views-v1)
  (get-views-v1 "clients" "8a81868e6808ec4501681508bcd530df")
  (get-views-v1 "clients" "8a81868e6808ec4501681508bcd530df" {:limit 500 :offset 13})
  
  (get-views-v1 "savings" "8a818684701c9a660170339c93f93004")
  (get-views-v1 "savings" "8a818684701c9a660170339c93f93004" {:limit 500})
  
  )

