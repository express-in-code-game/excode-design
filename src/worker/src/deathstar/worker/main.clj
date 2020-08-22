(ns deathstar.worker.main
  (:require
   [cljctools.net.server.api :as net.server.api]
   [cljctools.net.socket.api :as net.ws.api]
   [deathstar.worker.app :as app.api]
   [cljctools.nrepl.api :as nrepl.api]
   ))

(def channels (merge
               (net.server.api/create-channels)
               (net.ws.api/create-channels)
               (nrepl.api/create-channels)))

(def net-server (net.server.api/create-proc-server channels {} {:ws? true}))

(def nrepl-server (nrepl.api/create-proc-ops channels {} {::nrepl.api/host "0.0.0.0"
                                                          ::nrepl.api/port 7799}))


(defn -main [& args]
  (println "deathstar.worker.main/-main")
  (net.server.api/start net-server)
  (nrepl.api/start nrepl-server))

(comment

  (do
    (net.server.api/stop net-server)
    (def net-server (net.server.api/create-proc-server channels {} {:ws? true}))
    (net.server.api/start net-server))

  (def net-ws (net.ws.api/create-proc-ws net-ws|| {} {:url "ws://0.0.0.0:8080/ws"}))
  (net.ws.api/send-data net-ws {:a 1})

  ;;
  )
