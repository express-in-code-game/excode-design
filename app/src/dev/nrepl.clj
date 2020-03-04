(ns dev.nrepl
  (:require
   [nrepl.server :refer [start-server stop-server]]
   [clojure.repl :refer :all]
   [cider.nrepl :refer [cider-nrepl-handler]]
   ;
   ))

(defn start-nrepl-server [host port]
  (println (str "; started nREPL server on " host ":" port))
  (start-server
   :bind host
   :port port
   :handler cider-nrepl-handler #_(nrepl-handler)
   :middleware '[]))