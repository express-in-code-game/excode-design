(ns deathstar.server.main
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.string :as str]
   [clojure.tools.cli :refer [parse-opts]]

   [cljctools.csp.op.spec :as op.spec]
   
   [cljctools.net.server.spec :as net.server.spec]
   [cljctools.net.server.chan :as net.server.chan]
   [cljctools.net.server.impl :as net.server.impl]

   [cljctools.nrepl.spec :as nrepl.spec]
   [cljctools.nrepl.chan :as nrepl.chan]
   [cljctools.nrepl.impl :as nrepl.impl]

   [deathstar.hub.chan :as hub.chan]
   [deathstar.hub.impl :as hub.impl]

   [deathstar.server.spec :as server.spec]
   [deathstar.server.chan :as server.chan]

   [deathstar.scenario.rovers1.player]))

(def state (atom {}))

(def channels (merge
               (server.chan/create-channels)
               (net.server.chan/create-channels)
               (nrepl.chan/create-channels)
               (hub.chan/create-channels)))

(def net-server (net.server.impl/create-proc-ops channels
                                                 {::net.server.spec/with-websocket-endpoint? true}))

(def nrepl-server (nrepl.impl/create-proc-ops channels
                                              {::nrepl.spec/host "0.0.0.0"
                                               ::nrepl.spec/port 7799}))

(def hub (hub.impl/create-proc-ops channels {}))

#_(def gamestate (gamestate.api/create-proc-ops channels {}))

(def cli-options
  [["-s" "--settings FILEPATH" "Path to settings file (.edn) "
    :id :settings
    :parse-fn str
    :default "~/.deathstar/settings.edn"]])

(defn create-proc-ops
  [channels ctx]
  (let [{:keys [::server.chan/ops|m]} channels
        ops|t (tap ops|m (chan 10))]
    (go
      (loop []
        (when-let [[v port] (alts! [ops|t])]
          (condp = port
            ops|t
            (condp = (select-keys v [::op.spec/op-key ::op.spec/op-type])

              {::op.spec/op-key ::server.chan/init}
              (let [{:keys [::server.spec/options]} v
                    home-dir (System/getProperty "user.home")
                    fpath (str/replace (:settings options) #"~" home-dir)
                    settings (slurp fpath)]
                (println ::init)
                #_(println settings)
                ; read config etc
                (net.server.chan/op
                 {::op.spec/op-key ::net.server.chan/start-server}
                 channels)
                (nrepl.chan/op
                 {::op.spec/op-key ::nrepl.chan/start-server}
                 channels)))))
        (recur)))))

(def proc-main (create-proc-ops channels {}))

(defn -main [& args]
  (let [data (parse-opts args cli-options)]
    (println ::-main)
    (println (:options data))
    (server.chan/op
     {::op.spec/op-key ::server.chan/init}
     channels
     (:options data))))

(comment

  (do
    (net.server.api/stop net-server)
    (def net-server (net.server.api/create-proc-server channels {} {:ws? true}))
    (net.server.api/start net-server))

  (def net-ws (net.ws.api/create-proc-ws net-ws|| {} {:url "ws://0.0.0.0:8080/ws"}))
  (net.ws.api/send-data net-ws {:a 1})

  ;;
  )
