(ns deathstar.server.main
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [cljctools.net.server.api :as net.server.api]
   [cljctools.net.socket.api :as net.ws.api]

   [cljctools.nrepl.api :as nrepl.api]

   [clojure.string :as str]
   [clojure.tools.cli :refer [parse-opts]]

   [deathstar.server.ops :as ops.api]
   [deathstar.multiplayer.hub :as hub.api]
   #_[deathstar.gamestate.api :as gamestate.api]
   [deathstar.multiplayer.remote :as user.api]
   [deathstar.scenario.rovers1.player]))

(def channels (merge
               {::main| (chan 10)}
               (net.server.api/create-channels)
               (net.ws.api/create-channels)
               (nrepl.api/create-channels)
               (hub.api/create-channels)))

(def net-server (net.server.api/create-proc-server channels {} {:ws? true}))

(def nrepl-server (nrepl.api/create-proc-ops channels {} {::nrepl.api/host "0.0.0.0"
                                                          ::nrepl.api/port 7799}))

(def hub (hub.api/create-proc-ops channels {}))

#_(def gamestate (gamestate.api/create-proc-ops channels {}))

(def cli-options
  [["-s" "--settings FILEPATH" "Path to settings file (.edn) "
    :id :settings
    :parse-fn str
    :default "~/.deathstar/settings.edn"]])

(defn create-proc-main
  [channels ctx]
  (let [{:keys [::main|]} channels]
    (go
      (loop []
        (when-let [[v port] (alts! [main|])]
          (condp = port
            main|
            (condp = (:op v)

              ::start
              (let [{:keys [options]} v
                    home-dir (System/getProperty "user.home")
                    fpath (str/replace (:settings options) #"~" home-dir)
                    settings (slurp fpath)]
                (println ::main| ::start)
                #_(println settings)
                ; read config etc
                (net.server.api/start net-server)
                (nrepl.api/start nrepl-server)))))
        (recur)))))

(def proc-main (create-proc-main channels {}))

(defn -main [& args]
  (let [data (parse-opts args cli-options)]
    (println ::-main)
    (println (:options data))
    (put! (::main| channels) {:op ::start :options (:options data)})))

(comment

  (do
    (net.server.api/stop net-server)
    (def net-server (net.server.api/create-proc-server channels {} {:ws? true}))
    (net.server.api/start net-server))

  (def net-ws (net.ws.api/create-proc-ws net-ws|| {} {:url "ws://0.0.0.0:8080/ws"}))
  (net.ws.api/send-data net-ws {:a 1})

  ;;
  )
