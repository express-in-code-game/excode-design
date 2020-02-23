(ns app.main
  (:require [dev.nrepl :refer [start-nrepl-server]]
            [app.core]
            [app.kafka.wordcount-example]
            [app.kafka.streams-example]
            [app.kafka.transit-example]
            [app.kafka.ktable-agg-example]
            [app.kafka.reduce-example]
            [app.repl-interface.core]
   ;
            ))

(defn -main  [& args]
  (start-nrepl-server "0.0.0.0" 7788))