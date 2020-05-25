(ns alda-v1.output-server
  (:require [alda-v1.outputter :as outputter]
            [clojure.java.io   :as io]
            [io.pedestal.http  :as http]
            [jsonista.core     :as json]))

(defn- successful-response
  [m]
  {:status 200, :body (json/write-value-as-string m)})

(defn v1-output
  [{:keys [body]}]
  (successful-response
    (try
      (let [filename (slurp body)]
        {:success? true
         :output   (with-out-str (outputter/v1-output filename))})
      (catch Throwable t
        (prn t)
        {:success?      false
         :error-message (.getMessage t)}))))

(defn -main []
  (let [server    (-> {::http/routes #{["/" :post `v1-output]}
                       ::http/port   0
                       ::http/type   :jetty}
                      http/default-interceptors
                      http/create-server)
        _         (future (http/start server))
        ;; hack to ensure that the server has started before we see what port
        ;; it's listening on
        _         (Thread/sleep 500)
        port      (-> server
                      ::http/server
                      .getConnectors
                      first
                      .getLocalPort)
        port-file (io/file ".v1-server-port")]
    (println (format "Serving on port %d..." port))
    (spit port-file port)
    (.deleteOnExit port-file)
    ;; Shut down after 10 minutes
    (Thread/sleep (* 1000 60 10))
    (println "Shutting down.")
    (System/exit 0)))

