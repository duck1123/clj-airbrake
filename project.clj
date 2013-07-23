(defproject clj-airbrake "2.0.1-SNAPSHOT"
  :description "Airbrake Client"
  :url "https://github.com/duck1123/clj-airbrake"
  :min-lein-version "2.0.0"
  :dependencies [[clj-http "0.7.5"]
                 [clj-stacktrace "0.2.6"]
                 [ring/ring-core "1.2.0"]
                 [org.clojure/data.zip "0.1.1"]
                 [org.clojure/clojure "1.5.1"]
                 [hiccup "1.0.4"]]
  :profiles {:dev
             {:dependencies [[enlive "1.0.0-SNAPSHOT"]
                             [midje "1.6-alpha2"]]}}
  :plugins [[lein-midje     "3.0-beta1"]]
)
