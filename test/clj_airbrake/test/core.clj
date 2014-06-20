(ns clj-airbrake.test.core
  (:use [clj-airbrake.core] :reload)
  (:use clojure.data.zip.xml
        clojure.test
        midje.sweet)
  (:require [clojure.zip :as zip]
            [clojure.xml :as xml]
            [clj-http.client :as client]))

(defn- parse-xml [xml-str]
  (-> xml-str java.io.StringReader. org.xml.sax.InputSource. xml/parse zip/xml-zip))

(defn- make-notice-zip [& args]
  ;(println (apply make-notice args))
  (parse-xml (apply make-notice args)))

(defn- backtrace-lines [notice-xml]
  (map :attrs (xml-> notice-xml :error :backtrace :line zip/node)))

(defn- text-in
  [notice-xml path]
  (first (apply xml-> notice-xml (conj path text))))

(defn- var-elems-at
  "Extracts key-values (into a map) from XML blocks like:
    <cgi-data>
      <var key='SERVER_NAME'>example.org</var>
      <var key='HTTP_USER_AGENT'>Mozilla</var>
    </cgi-data>
"
  [notice-xml path]
  (let [pairs (for [var-elem (apply xml-> notice-xml (conj path :var zip/node))]
                [(get-in var-elem [:attrs :key]) (first (:content var-elem))])]
    (apply hash-map (flatten pairs))))






(fact "make-notice"
  (let [exception (Exception. "Foo")
        request {:url "http://example.com",
                 :component :foo,
                 :action :bar,
                 ;; note the symbols... prxml has issues
                 :cgi-data {"SERVER_NAME" "nginx",
                            "HTTP_USER_AGENT" "Mozilla"}
                 :params {"city" "LA",
                          "state" "CA"}
                 :session {:user-id "23",
                           :something-that-needs-escaping "<foo> \"&\"' </foo>"}}
        notice-xml (make-notice-zip "my-api-key"
                                    :production "/testapp"
                                    exception request)]

    (doseq [[expected-text path]
            [
             ["my-api-key"               [:api-key]]
             ["java.lang.Exception"      [:error :class]]
             ["java.lang.Exception: Foo" [:error :message]]
             ["/testapp"                 [:server-environment :project-root]]
             ["production"               [:server-environment :environment-name]]
             ["http://example.com"       [:request :url]]
             ["foo"                      [:request :component]]
             ["bar"                      [:request :action]]]
            ]
      (text-in notice-xml path) => expected-text)

    (are [expected-vars path] (= expected-vars (var-elems-at notice-xml path))
         (:cgi-data request) [:request :cgi-data]
         (:params request) [:request :params]
         ;; notice how the keywords get `name` called on them
         {"user-id" "23",
          "something-that-needs-escaping" "&lt;foo&gt; &quot;&amp;&quot;&apos; &lt;/foo&gt;"}
         [:request :session])

    (fact "backtraces"
      (let [first-line (first (backtrace-lines notice-xml))]
        (:file first-line) => "core.clj"
        (:method first-line) => "clj-airbrake.test.core/fn[fn]"
        (re-matches #"^\d+$" (:number first-line)) => truthy)))

  (fact "when no request is provided"
    (let [notice-xml (make-notice-zip "my-api-key" "test" "/testapp" (Exception. "foo"))]
      (xml-> notice-xml :request) => empty?))
  (fact "when a request is provided but no URL"
    (let [notice-xml-args ["my-api-key" "test" "/testapp" (Exception. "foo") {:action "foo"}]]
      (is (thrown-with-msg? IllegalArgumentException #"url is required"
            (apply make-notice notice-xml-args)))))
  (fact "when no session, cgi, or params are provided"
    (let [notice-xml (make-notice-zip "my-api-key" "test" "/testapp"
                                      (Exception. "foo")
                                      {:url "foo" :session nil :params {}})]
      (xml-> notice-xml :request)       =not=> empty?
      (xml-> notice-xml :request :session)  => empty?
      (xml-> notice-xml :request :params)   => empty?
      (xml-> notice-xml :request :cgi-data) => empty?))

  (fact "when a message prefix is added"
    (let [notice-xml (make-notice-zip "my-api-key" "test" "/testapp"
                                      (Exception. "Foo") {:url "foo"} "bar")]
      (text-in notice-xml [:error :message]) => "bar java.lang.Exception: Foo")))

(fact "send-notice"
  (send-notice "<notice>...</notice>") =>
  {:error-id "2285317953"
   :id "100"
   :url "http://sub.airbrakeapp.com/errors/42/notices/100"}
  #_(fake (client/post
         "http://airbrakeapp.com/notifier_api/v2/notices"
         {:body "<notice>...</notice>", :content-type :xml, :accept :xml}) =>

         {:status 200, :headers {"server" "nginx/0.6.35"},
          :body "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<notice>\n  <error-id type=\"integer\">2285317953</error-id>\n  <url>http://sub.airbrakeapp.com/errors/42/notices/100</url>\n  <id type=\"integer\">100</id>\n</notice>\n"}))
