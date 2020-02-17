(ns lab.main
  (:require [pad.nrepl.core :refer [start-nrepl-server]]
            [lab.core]
            [lab.streams-example]
   ;
            ))

(defn -main  [& args]
  (start-nrepl-server "0.0.0.0" 7788))