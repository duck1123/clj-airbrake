(defproject clj-airbrake "2.0.1-SNAPSHOT"
  :description "Airbrake Client"
  :url "https://github.com/duck1123/clj-airbrake"
  :min-lein-version "2.0.0"
  :dependencies [[clj-http "0.2.7"]
                 [clj-stacktrace "0.2.4"]
                 [ring/ring-core "0.3.6"]
                 [org.clojure/data.zip "0.1.0"]
                 [org.clojure/clojure "1.5.0"]
                 [hiccup "1.0.0"]]
  :profiles {:dev
             {:dependencies [[lein-clojars "0.6.0"]
                             [enlive "1.0.0-SNAPSHOT"]
                             [midje "1.5-RC1"]]}})
