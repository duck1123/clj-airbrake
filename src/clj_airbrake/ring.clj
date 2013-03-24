(ns clj-airbrake.ring
  (:use clj-airbrake.core))

(defn request-to-message
  "Maps the ring request map to the format of the airbrake params"
  [req]
  {:url (str (name (:scheme req))
             "://"
             (:server-name req)
             (:uri req))
   :component "component"
   :action "action"
   :cgi-data (get req :headers {})
   :params (or (:params req)
               {:query-string (:query-string req)})
   :session (get req :sesion {})})

(defn wrap-airbrake
  "Catches exceptions and sends Airbrake notification."
  ([handler api-key]
     (wrap-airbrake handler api-key "development"))
  ([handler api-key environment-name]
     (wrap-airbrake handler api-key environment-name request-to-message))
  ([handler api-key environment-name request-mapper]
     (fn [req]
       (try (handler req)
            (catch Exception e
              (let [dir (System/getProperty "user.dir")
                    request (request-mapper req)]
                ;; (println "exception caught")
                ;; (println "api key: " api-key)
                ;; (println "environment name: " environment-name)
                ;; (println "dir: " dir)
                ;; (println "request: " request)
                (notify api-key environment-name dir
                        e request)
                (throw e)))))))
