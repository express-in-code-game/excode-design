(ns deathstar.worker.main
  (:require
   [cljctools.net.server.api :as net.server.api]
   [cljctools.net.socket.api :as net.ws.api]
   [deathstar.worker.app :as app.api]))

(def net-server|| (net.server.api/create-channels))

(def net-server (net.server.api/create-proc-server net-server|| {} {:ws? true}))

(def net-ws|| (net.ws.api/create-channels))

(defn -main [& args]
  (println "deathstar.worker.main/-main")
  (net.server.api/start net-server))

(comment

  (do
    (net.server.api/stop net-server)
    (def net-server (net.server.api/create-proc-server net-server|| {} {:ws? true}))
    (net.server.api/start net-server))

  (def net-ws (net.ws.api/create-proc-ws net-ws|| {} {:url "ws://0.0.0.0:8080/ws"}))
  (net.ws.api/send-data net-ws {:a 1})

  ;;
  )
