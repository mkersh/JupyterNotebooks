(defproject my-awesome-gui-application "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [
      [org.clojure/clojure "1.10.1"]
      [org.clojure/data.json "1.0.0"]
      [http-kit "2.3.0"]
  ]
  jvm-opts ["--add-modules" "java.xml.bind"]
)
