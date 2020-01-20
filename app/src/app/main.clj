(ns app.main
  (:require [app.nrepl :refer [start-nrepl-server]]
            [app.core]
            [app.streams-example]
            [app.kafka-events]
   ;
            ))

(defn -main  [& args]
  (start-nrepl-server "0.0.0.0" 7788))