(ns http.api.branch
  (:require [http.api.json_helper :as api]))

(defn list-branches []
  (let [options {:basic-auth (api/get-auth "env1")
                 :headers {"Accept" "application/vnd.mambu.v2+json"}
                 :query-params {"detailsLevel" "FULL"}}
        url "{{env1}}/branches"]
    (api/PRINT (api/GET url options))))

; Test in your REPL: Select line to run ctl+alt+c <space>
; Use api/find-path and api/extract-attrs to navigate through results
(comment
  (time (list-branches))
  )