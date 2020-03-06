(ns alda-v1.output-server
  (:require [alda-v1.outputter :as outputter]
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

(defn -main [port*]
  (let [port (Integer/parseInt port*)]
    (println (format "Starting server on port %d..." port))
    (future
      (-> {::http/routes #{["/" :post `v1-output]}
           ::http/port   port
           ::http/type   :jetty}
          http/default-interceptors
          http/create-server
          http/start)))
  ;; Shut down after 2 minutes. This server isn't intended to be around for a
  ;; long time, just long enough to run v1/bin/output on a smallish number of
  ;; Alda source files.
  (Thread/sleep (* 1000 60 2))
  (println "Shutting down.")
  (System/exit 0))

