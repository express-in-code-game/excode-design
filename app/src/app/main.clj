(ns app.main
  (:require [app.nrepl :refer [start-nrepl-server]]
            [app.core]
            [app.kafka.wordcount-example]
            [app.kafka.streams-example]
            [app.kafka.queries]
            [app.kafka.repl-kafka-events]
            [app.kafka.serdes]
   ;
            ))

(defn -main  [& args]
  (start-nrepl-server "0.0.0.0" 7788))