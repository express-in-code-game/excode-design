(ns app.main
  (:require [app.nrepl :refer [start-nrepl-server]]
            [app.core]
            [app.kafka.serdes] ; goes first for deftypes to be :imported
            [app.kafka.wordcount-example]
            [app.kafka.streams-example]
            [app.kafka.transit]
            [app.kafka.repl-kafka-events]
   ;
            ))

(defn -main  [& args]
  (start-nrepl-server "0.0.0.0" 7788))