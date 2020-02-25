(ns alda-v1.outputter
  (:require [alda.parser :as parser]
            [clojure.math.numeric-tower :as math])
  (:import [alda.lisp.model.records Note]))

(defn- handle-error
  [x]
  (if (instance? Throwable x)
    (throw x)
    x))

(defn output
  [{:keys [events]}]
  (println "offset,duration,midi note")
  (doseq [event events
          :when (or (instance? Note event)
                    (throw (ex-info "Unexpected event type"
                                    {:event event
                                     :type (type event)})))
          :let [{:keys [offset duration midi-note]} event]]
    (println (format "%s,%s,%s"
                     (math/round offset)
                     (math/round duration)
                     midi-note))))

(defn -main [filename]
  (-> filename slurp parser/parse-input handle-error output))
