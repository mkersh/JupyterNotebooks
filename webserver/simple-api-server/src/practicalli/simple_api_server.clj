(ns practicalli.simple-api-server
  (:gen-class)
  (:require [org.httpkit.server :as server]))

(defn handler
  "A function that handles all requests from the server.
  Arguments: `req` is a ring request hash-map
  Return: ring response hash-map including :status :headers and :body"
  [req]
  {:status  200
  :headers {}
  :body    "XXXXHello Clojure Server world!"})

(defn create-server
  "A ring-based server listening to all http requests
  port is an Integer greater than 128"
  [port]
  (server/run-server handler {:port port}))




(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!")
  (create-server 8080)
  )
