(ns app.main
  (:require [app.nrepl :refer [start-nrepl-server]]
            [app.core]
            [app.kafka.streams-example]
            [app.kafka.repl-kafka-events]
            [app.kafka.wordcount]
   ;
            ))

(defn -main  [& args]
  (start-nrepl-server "0.0.0.0" 7788))