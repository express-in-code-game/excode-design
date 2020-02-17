(ns app.main
  (:require [pad.nrepl.core :refer [start-nrepl-server]]
            [app.core]
            [app.streams-example]
   ;
            ))

(defn -main  [& args]
  (start-nrepl-server "0.0.0.0" 7788))