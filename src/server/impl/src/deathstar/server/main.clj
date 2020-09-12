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

   [cljctools.nrepl.server.spec :as nrepl.server.spec]
   [cljctools.nrepl.server.chan :as nrepl.server.chan]
   [cljctools.nrepl.server.impl :as nrepl.server.impl]

   [deathstar.hub.chan :as hub.chan]
   [deathstar.hub.impl :as hub.impl]

   [deathstar.server.spec :as server.spec]
   [deathstar.server.chan :as server.chan]

   [deathstar.scenario.rovers1.player]))


(def channels (merge
               (server.chan/create-channels)
               (net.server.chan/create-channels)
               (nrepl.server.chan/create-channels)
               (hub.chan/create-channels)))

(def http-chan-interceptor
  {:name ::http-chan
   :enter
   (fn [ctx]
     (go
       (let [request-value (get-in ctx [:request :edn-params])
             out| (chan 1)
             _ (do
                 (println ::request-value (type request-value))
                 (println ::request-value request-value))
             _ (put! (::hub.chan/ops| channels) (assoc request-value ::op.spec/out| out|))
             response-value (<! out|)]
         #_(put! (::hub.chan/response| channels) response-value)
   
         (println ::response-value response-value)
         (assoc ctx :response {:body response-value :status 200}))))})

(def net-server (net.server.impl/create-proc-ops
                 channels
                 {::net.server.spec/with-websocket-endpoint? true
                  ::net.server.spec/routes
                  #{["/http-chan" :post (conj net.server.impl/common-interceptors http-chan-interceptor) :route-name ::http-chan]
                    ["/" :get (fn [_] {:body (clojure-version) :status 200}) :route-name :root]
                    ["/echo" :get #(hash-map :body (pr-str %) :status 200) :route-name :echo]}}))

(def nrepl-server (nrepl.server.impl/create-proc-ops channels
                                              {::nrepl.server.spec/host "0.0.0.0"
                                               ::nrepl.server.spec/port 7799}))

(def hub-state (hub.impl/create-state))

(def hub (hub.impl/create-proc-ops channels hub-state {}))

#_(def gamestate (gamestate.api/create-proc-ops channels {}))

(def cli-options
  [["-s" "--config FILEPATH" "Path to config file "
    :id ::config
    :parse-fn str
    :default "~/foo.edn"]])

(defn read-file
  [filepath]
  (let [home-dir (System/getProperty "user.home")
        filepath* (str/replace filepath #"~" home-dir)]
    (slurp filepath*)))

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
              (let [{:keys [::options]} v]
                (println ::init)
                #_(println (read-file (::config options)))
                ; read config etc
                (net.server.chan/op
                 {::op.spec/op-key ::net.server.chan/start-server}
                 channels)
                (nrepl.server.chan/op
                 {::op.spec/op-key ::nrepl.server.chan/start-server}
                 channels)))))
        (recur)))))

(def proc-main (create-proc-ops channels {}))

(defn -main [& args]
  (let [cli-options* (parse-opts args cli-options)]
    (println ::-main)
    (println (:options cli-options*))
    (server.chan/op
     {::op.spec/op-key ::server.chan/init}
     channels
     {::options (:options cli-options*)})))

(comment

  (do
    (net.server.api/stop net-server)
    (def net-server (net.server.api/create-proc-server channels {} {:ws? true}))
    (net.server.api/start net-server))

  (def net-ws (net.ws.api/create-proc-ws net-ws|| {} {:url "ws://0.0.0.0:8080/ws"}))
  (net.ws.api/send-data net-ws {:a 1})

  ;;
  )
