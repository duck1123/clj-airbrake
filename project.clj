(defproject clj-airbrake "2.0.1-SNAPSHOT"
  :description "Airbrake Client"
  :url "https://github.com/duck1123/clj-airbrake"
  :min-lein-version "2.0.0"
  :dependencies [[clj-http "0.7.2"]
                 [clj-stacktrace "0.2.5"]
                 [ring/ring-core "1.2.0-beta3"]
                 [org.clojure/data.zip "0.1.0"]
                 [org.clojure/clojure "1.5.1"]
                 [hiccup "1.0.0"]]
  :profiles {:dev
             {:dependencies [[lein-clojars "0.6.0"]
                             [enlive "1.0.0-SNAPSHOT"]
                             [midje "1.5-RC1"]]}}
  :plugins [[lein-midje     "3.0-beta1"]]
)
