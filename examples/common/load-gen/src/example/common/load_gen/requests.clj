(ns example.common.load-gen.requests
  "Functions for sending a timed sequence of HTTP requests."
  (:require [clj-http.client :as client]
            [clojure.tools.logging.readable :as log]))


(defn- do-request
  "Send an HTTP request asynchronously"
  [conn-mgr client req]
  (log/debugf "Sending request : %s" req)
  (try
    (client/request (conj req
                          {:async true
                           :throw-exceptions false
                           :connection-manager conn-mgr
                           :http-client client
                           :multi-param-style :comma-separated})
                    (fn [response]
                      (log/debugf "Received response : %s"
                                  {:request  req
                                   :response (select-keys response [:status :body])}))
                    (fn [error]
                      (log/debugf error "Error processing request : %s" req)))
    (catch Exception e
      (log/debugf e "Error sending request : %s" req))))



(defn- sleep-until
  "Sleep the current thread until system time t."
  [t]
  (let [^long d (- t (System/currentTimeMillis))]
    (when (pos? d)
      (Thread/sleep d))))



(defn do-requests
  "Given a (possibly infinite) seq of vectors `signal`, for each vector [t req]
   perform an async HTTP req at system time t. It is assumed t monotonically
   increases in the sequence."
  [conn-mgr client signal]
  (future (doseq [[t req] signal]
            (sleep-until t)
            (do-request conn-mgr client req))))
