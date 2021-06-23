(ns deathstar.app.main
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.core.async.impl.protocols :refer [closed?]]
   [clojure.string :as str]
   [cljs.core.async.interop :refer-macros [<p!]]
   [goog.string.format :as format]
   [goog.string :refer [format]]
   [goog.object]
   [cljs.reader :refer [read-string]]

   [cljctools.csp.op.spec :as op.spec]
   [cljctools.cljc.core :as cljc.core]

   [cljctools.rsocket.spec :as rsocket.spec]
   [cljctools.rsocket.chan :as rsocket.chan]
   [cljctools.rsocket.impl :as rsocket.impl]
   [cljctools.rsocket.examples]

   [deathstar.app.spec :as app.spec]
   [deathstar.app.chan :as app.chan]

   [deathstar.app.tournament.spec :as app.tournament.spec]
   [deathstar.app.tournament.chan :as app.tournament.chan]
   [deathstar.app.tournament.impl :as app.tournament.impl]


   [deathstar.ui.tournament.spec :as ui.tournament.spec]
   [deathstar.ui.tournament.chan :as ui.tournament.chan]

   [cljctools.peernode.spec :as peernode.spec]
   [cljctools.peernode.chan :as peernode.chan]

   [deathstar.scenario-api.spec :as scenario-api.spec]
   [deathstar.scenario-api.chan :as scenario-api.chan]

   [deathstar.ui.spec :as ui.spec]
   [deathstar.ui.chan :as ui.chan]))

(defonce fs (js/require "fs"))
(defonce path (js/require "path"))
(defonce axios (.-default (js/require "axios")))
(defonce puppeteer (js/require "puppeteer-core"))
(defonce OrbitDB (js/require "orbit-db"))
(defonce IpfsClient (js/require "ipfs-http-client"))
(defonce http (js/require "http"))
(defonce Url (js/require "url"))
(defonce express (js/require "express"))
(defonce cors (js/require "cors"))
(defonce bodyParser (js/require "body-parser"))
(defonce ws (js/require "ws"))
(defonce WSServer (.-Server ws))

(defonce channels (merge
                   (app.chan/create-channels)
                   (ui.chan/create-channels)
                   (app.tournament.chan/create-channels)
                   (peernode.chan/create-channels)))

#_(defonce channels-rsocket-peernode (rsocket.chan/create-channels))
(defonce channels-rsocket-ui (rsocket.chan/create-channels))
(defonce channels-rsocket-scenario (rsocket.chan/create-channels))
(defonce channels-rsocket-player (rsocket.chan/create-channels))

#_(do
    (pipe (::peernode.chan/ops| channels) (::rsocket.chan/ops| channels-rsocket-peernode))
    (pipe (::rsocket.chan/requests| channels-rsocket-peernode) (::app.chan/ops| channels))
    (defonce rsocket-peernode (rsocket.impl/create-proc-ops
                               channels-rsocket-peernode
                               {::rsocket.spec/connection-side ::rsocket.spec/initiating
                                ::rsocket.spec/host "peernode"
                                ::rsocket.spec/port 7000
                                ::rsocket.spec/transport ::rsocket.spec/websocket})))

(pipe (::app.tournament.chan/ops| channels) (::app.chan/ops| channels))

(pipe (::ui.chan/ops| channels) (::rsocket.chan/ops| channels-rsocket-ui))
(go (loop []
      (when-let [value (<! (::rsocket.chan/requests| channels-rsocket-ui))]
        (let [{:keys [::op.spec/op-key]} value]
          (cond
            (isa? op-key ::app.chan/op) (put! (::app.chan/ops| channels) value)
            (isa? op-key ::scenario-api.chan/op) (put! (::rsocket.chan/ops| channels-rsocket-scenario) value)))
        (recur))))

#_(defonce rsocket-ui (rsocket.impl/create-proc-ops
                 channels-rsocket-ui
                 {::rsocket.spec/connection-side ::rsocket.spec/accepting
                  ::rsocket.spec/host "0.0.0.0"
                  ::rsocket.spec/port 7001
                  ::rsocket.spec/transport ::rsocket.spec/websocket}))

(pipe (::rsocket.chan/requests| channels-rsocket-player) (::rsocket.chan/ops| channels-rsocket-scenario))
#_(defonce rsocket-scenario (rsocket.impl/create-proc-ops
                       channels-rsocket-scenario
                       {::rsocket.spec/connection-side ::rsocket.spec/accepting
                        ::rsocket.spec/host "0.0.0.0"
                        ::rsocket.spec/port 7002
                        ::rsocket.spec/transport ::rsocket.spec/websocket}))

(go (loop []
      (when-let [value (<! (::rsocket.chan/requests| channels-rsocket-scenario))]
        (let [{:keys [::op.spec/op-key]} value]
          (cond
            (isa? op-key ::app.chan/op) (put! (::app.chan/ops| channels) value)
            :else (put! (::rsocket.chan/ops| channels-rsocket-player) value)))
        (recur))))

#_(defonce rsocket-player (rsocket.impl/create-proc-ops
                     channels-rsocket-player
                     {::rsocket.spec/connection-side ::rsocket.spec/accepting
                      ::rsocket.spec/host "0.0.0.0"
                      ::rsocket.spec/port 7003
                      ::rsocket.spec/transport ::rsocket.spec/websocket}))

(defonce ctx {::app.spec/state*
              (atom
               {::app.spec/peer-id nil
                ::app.spec/tournaments {}
                ::app.spec/games {}
                ::app.spec/scenarios {}
                ::app.spec/tournament-metas {}
                ::app.spec/peer-metas {}})

              ::app.spec/tournaments* (atom {})
              ::app.spec/games* (atom {})
              ::app.spec/scenarios* (atom {})

              ::app.spec/websocket-servers* (atom {})
              ::app.spec/rsockets* (atom {})
              
              ::app.spec/app-eventlog* (atom nil)
              ::app.spec/browser* (atom nil)
              ::app.spec/ipfs* (atom nil)
              ::app.spec/orbitdb* (atom nil)
              ::app.spec/TOPIC-ID "deathstar-1a58070"})

(defonce _ (add-watch (::app.spec/state* ctx) ::watch
                      (fn [k atom-ref oldstate newstate]
                        (ui.chan/op
                         {::op.spec/op-key ::ui.chan/update-state
                          ::op.spec/op-type ::op.spec/fire-and-forget}
                         channels
                         newstate))))

(def HTTP_PORT 8000)

(def app (express))
(def server (.createServer http app))

(.use app (cors))
(.use app (.text bodyParser #js {"type" "text/plain" #_"*/*"
                                 "limit" "100kb"}))

(.listen server HTTP_PORT)


(defn create-rsocket
  [request socket head opts]
  (let [{:keys [::app.spec/frequency]} opts
        {:keys [::app.spec/websocket-servers*
                ::app.spec/rsockets*]} ctx
        websocket-server (WSServer. #js {"noServer" true})
        rsocket|| (rsocket.chan/create-channels)
        rsocket-proc (rsocket.impl/create-proc-ops
                      channels-rsocket-scenario
                      {::rsocket.spec/connection-side ::rsocket.spec/accepting
                       ::rsocket.spec/transport ::rsocket.spec/websocket
                       ::rsocket.spec/create-websocket-server (fn [options]
                                                                websocket-server)})]
    (.on socket "close"
         (fn []
           (go
             (<! (rsocket.chan/op
                  {::op.spec/op-key ::rsocket.spec/release
                   ::op.spec/op-type ::op.spec/request-response
                   ::op.spec/op-orient ::op.spec/request}
                  rsocket||
                  {}))
             (swap! websocket-servers* dissoc frequency)
             (swap! rsockets* dissoc frequency))))
    (.handleUpgrade websocket-server request socket head
                    (fn done [ws]
                      (.emit websocket-server "connection" ws request)))
    (swap! websocket-servers* assoc frequency websocket-server)
    (swap! rsockets* assoc frequency rsocket||)))

(defn create-tournament-proc
  [ctx opts]
  (go
    (let [{:keys [::app.spec/state*
                  ::app.spec/ipfs*
                  ::app.spec/orbitdb*
                  ::app.spec/tournaments*
                  ::app.spec/games*
                  ::app.spec/scenarios*
                  ::app.spec/app-eventlog*
                  ::app.spec/TOPIC-ID]} ctx
          {:keys [::app.spec/frequency
                  ::app.spec/peer-name
                  ::app.spec/peer-id
                  ::app.spec/host-id]} opts
          own-peer-id (get @state* ::app.spec/peer-id)]
      (let [tournament|| (merge
                          (app.tournament.chan/create-channels)
                          (ui.tournament.chan/create-channels))
            tournament-proc (app.tournament.impl/create-proc-ops
                             tournament|| ctx opts)]

        (swap! tournaments* assoc frequency tournament||)))))

(.on server "upgrade"
     (fn [request socket head]
       (let [{:keys [pathname searchParams]}
             (js->clj (.parse Url (.-url request)) :keywordize-keys true)
             {:keys [::app.spec/websocket-servers*
                     ::app.spec/rsockets*
                     ::app.spec/tournaments*]} ctx]
         (cond
           (= pathname "/tournament-rsocket")
           (let [frequency (.get searchParams "frequency")
                 _ (create-rsocket request socket head {::app.spec/frequency frequency})
                 rsocket|| (get @rsockets* frequency)
                 _ (when-not (get @tournaments* frequency)
                     (create-tournament-proc ctx {::app.spec/frequency frequency}))
                 tournament|| (get @tournaments* frequency)]
             (pipe  (tap (::ui.tournament.chan/ops|m tournament||) (chan 10)) (::rsocket.chan/ops| rsocket||)))
           :else (.destroy socket)))))

(.get app "/"
      (fn [request response next]
        (go
          (<! (timeout 2000))
          (.send response "hello world"))))

(.get app "/tournament-rsocket/:id"
      (fn [request response next]
        (let [{:keys [id]
               :as params} (js->clj (.-params request)
                                    :keywordize-keys true)]
          (go
            (<! (timeout 1000))
            (.send response id)))))


(comment

  (js/Object.keys ipfs)
  (js/Object.keys ipfs.pubsub)

  (go
    (let [id (<p! (daemon._ipfs.id))]
      (println (js-keys id))
      (println (.-id id))
      (println (format "id is %s" id))))


  (def orbitdb @(::app.spec/orbitdb* ctx))
  (go
    (def eventlog (<p! (.eventlog orbitdb "foo")))
    (<p! (.load eventlog)))
  (go
    (println (<p! (.add eventlog (pr-str {::app.spec/peer-id (::app.spec/peer-id @(::app.spec/state* ctx))
                                          ::random-int 1  #_(rand-int 1000)})))))
  (-> eventlog
      (.iterator  #js {"limit" -1})
      (.collect)
      (.map (fn [e]
              (println (js-keys e))
              (println (js-keys (.-payload e)))
              (println (.-hash e))
              (println (.-next e))
              (read-string (.-value (.-payload e)))))
      #_(first)
      (println))

  (go
    (<p! (.drop eventlog)))

  (empty? #js [])


  (count @(::app.spec/tournaments* ctx))
  
  (let [events (-> (::eventlog @(get @(::app.spec/tournaments* ctx) "fbd5d2fb-6452-4546-8202-5280bfb8ffdc") )
                   (.iterator  #js {"limit" -1
                                    "reverse" false})
                   (.collect)
                   (vec))]
    (println ::count-tournament-events (count events)))

  ;;
  )

(declare init-puppeteer)

(defn create-proc-ops
  [channels ctx opts]
  (let [{:keys [::app.chan/ops|]} channels
        {:keys [::app.spec/state*
                ::app.spec/ipfs*
                ::app.spec/orbitdb*
                ::app.spec/tournaments*
                ::app.spec/games*
                ::app.spec/scenarios*
                ::app.spec/app-eventlog*
                ::app.spec/TOPIC-ID]} ctx

        create-tournament-proc
        (fn [opts]
          (go
            (let [{:keys [::app.spec/frequency
                          ::app.spec/peer-name
                          ::app.spec/peer-id
                          ::app.spec/host-id]} opts
                  own-peer-id (get @state* ::app.spec/peer-id)]
              (let [tournament|| (app.tournament.chan/create-channels)
                    tournament-proc (app.tournament.impl/create-proc-ops
                                     tournament|| ctx opts)]
                (swap! tournaments* assoc frequency tournament||)))))

        create-tournament-meta
        (fn [opts]
          (let [{:keys [::app.spec/frequency
                        ::app.spec/peer-name
                        ::app.spec/peer-id
                        ::app.spec/host-id]} opts
                tournament-meta (select-keys opts [::app.spec/frequency
                                                   ::app.spec/host-id])]
            (swap! state* update-in [::app.spec/tournament-metas]
                   assoc frequency tournament-meta)))

        remove-tournament-meta
        (fn [opts]
          (let [{:keys [::app.spec/frequency
                        ::app.spec/peer-id]} opts]
            (swap! state* update-in [::app.spec/tournament-metas]
                   dissoc frequency)))

        close-tournament-proc
        (fn [opts]
          (go
            (let [{:keys [::app.spec/frequency
                          ::app.spec/peer-id]} opts
                  own-peer-id (get @state* ::app.spec/peer-id)
                  tournament|| (get @tournaments* frequency)]
              (<! (app.tournament.chan/op
                   {::op.spec/op-key ::app.tournament.chan/release
                    ::op.spec/op-type ::op.spec/request-response
                    ::op.spec/op-orient ::op.spec/request}
                   tournament||
                   {::app.spec/frequency frequency
                    ::app.spec/peer-id peer-id}))
              (swap! tournaments* dissoc frequency)
              (swap! state* update-in [::app.spec/tournament-metas]
                     dissoc frequency))))]
    (go
      (loop []
        (when-let [[value port] (alts! [ops|])]
          (condp = port
            ops|
            (condp = (select-keys value [::op.spec/op-key ::op.spec/op-type ::op.spec/op-orient])

              {::op.spec/op-key ::app.chan/init
               ::op.spec/op-type ::op.spec/fire-and-forget}
              (println ::init)
              #_(let [{:keys []} value]
                  (println ::init)
                  (try
                    (let [ipfs (IpfsClient "http://ipfs:5001")]
                      (reset! ipfs* ipfs)
                      (swap! state* assoc ::app.spec/peer-id (.-id (<p! (.id ipfs))))
                      (reset! orbitdb* (<p! (.createInstance OrbitDB ipfs (clj->js {"directory" "/root/.orbitdb"}))))
                      (reset! app-eventlog* (<p! (.eventlog @orbitdb*
                                                            TOPIC-ID
                                                            (clj->js {"accessController"
                                                                      {"write" ["*"]}})))))
                    (let [app-eventlog @app-eventlog*]
                      #_(<p! (.drop (::eventlog @app-eventlog*)))
                      (<p! (.load app-eventlog))
                      (let [entries (-> app-eventlog
                                        (.iterator  #js {"limit" -1
                                                         "reverse" false})
                                        (.collect)
                                        (vec))]
                        (println ::count-app-events (count entries))
                        (doseq [entry entries]
                          (let [value (read-string (.-value (.-payload entry)))]
                            (put! ops| (merge value
                                              {::replay? true})))
                          #_(when (empty? (.-next entry))
                              (swap! app-eventlog*
                                     assoc
                                     ::eventlog-prev-hash
                                     (.-hash entry))
                              (close! done|))))

                      #_(.on (.-events app-eventlog)
                             "replicated"
                             (fn [address]
                               (println ::replicated)
                               #_(println  (->>
                                            (-> app-eventlog
                                                (.iterator  #js {"gt" (::eventlog-prev-hash @app-eventlog*)})
                                                (.collect))
                                            (map (fn [entry] (select-keys (read-string (.-value (.-payload entry)))
                                                                          [::app.spec/frequency
                                                                           ::app.spec/op-type])))))
                               (-> app-eventlog
                                   (.iterator  #js {"lt" (::eventlog-prev-hash @app-eventlog*)})
                                   (.collect)
                                   (.map (fn [entry]
                                           (let [value (read-string (.-value (.-payload entry)))]
                                             (put! ops| value))
                                           (when (empty? (.-next entry))
                                             (swap! app-eventlog*
                                                    assoc
                                                    ::eventlog-prev-hash
                                                    (.-hash entry))))))))
                      (.on (.-events app-eventlog)
                           "replicate.progress"
                           (fn [address hash entry progress have]
                             (println ::replicate-progress)
                             (println (read-string (.-value (.-payload entry))))
                             (let [value (read-string (.-value (.-payload entry)))]
                               (put! ops| value))))
                      #_(.on (.-events app-eventlog)
                             "write"
                             (fn [address entry heads]
                               #_(swap! app-eventlog*
                                        assoc
                                        ::eventlog-prev-hash
                                        (.-hash entry)))))
                    (catch js/Error err (println err)))
                  (let [ipfs @ipfs*
                        id (.-id (<p! (.id ipfs)))
                        text-encoder (js/TextEncoder.)
                        text-decoder (js/TextDecoder.)]
                    (.subscribe (.-pubsub ipfs)
                                TOPIC-ID
                                (fn [msg]
                                  (when-not (= id (.-from msg))
                                    (swap! state* assoc-in [::app.spec/peer-metas (.-from msg)]
                                           (merge
                                            (read-string (.decode text-decoder  (.-data msg)))
                                            {::app.spec/peer-id (.-from msg)
                                             ::app.spec/received-at (.now js/Date)}))
                                    (do
                                      #_(println (format "id: %s" id))
                                      #_(println (format "from: %s" (.-from msg)))
                                      #_(println (format "data: %s" (.decode text-decoder  (.-data msg))))
                                      #_(println (format "topicIDs: %s" msg.topicIDs))))))
                    (go (loop [counter 0]
                          (.publish (.-pubsub ipfs)
                                    TOPIC-ID
                                    (-> text-encoder
                                        (.encode  (pr-str {::app.spec/peer-id id
                                                           ::app.spec/counter counter}))))
                          (<! (timeout 2000))
                          (recur (inc counter))))
                    (go (loop []
                          (<! (timeout 4000))
                          (doseq [[peer-id {:keys [::app.spec/received-at]
                                            :as peer-meta}] (::app.spec/peer-metas   @state*)
                                  :when (> (- (.now js/Date) received-at) 8000)]
                            (println ::removing-peer)
                            (swap! state* update-in [::app.spec/peer-metas] dissoc peer-id))
                          (recur)))))

              {::op.spec/op-key ::app.chan/request-state-update
               ::op.spec/op-type ::op.spec/fire-and-forget}
              (let [{:keys []} value]
                (ui.chan/op
                 {::op.spec/op-key ::ui.chan/update-state
                  ::op.spec/op-type ::op.spec/fire-and-forget}
                 channels
                 @state*))

              {::op.spec/op-key ::app.tournament.chan/create-tournament
               ::op.spec/op-type ::op.spec/request-response
               ::op.spec/op-orient ::op.spec/request}
              (let [own-peer-id (get @state* ::app.spec/peer-id)
                    {:keys [::app.spec/peer-name
                            ::app.spec/frequency
                            ::app.spec/peer-id
                            ::app.spec/host-id
                            ::replay?
                            ::op.spec/out|]
                     :or {peer-name (str "peer" (rand-int 1000))
                          frequency (str (cljc.core/rand-uuid))
                          peer-id own-peer-id
                          host-id own-peer-id}} value]
                (when (not (get @tournaments* frequency))
                  (when (= own-peer-id peer-id)
                    (<! (create-tournament-proc {::app.spec/frequency frequency
                                                 ::app.spec/peer-name peer-name
                                                 ::app.spec/peer-id peer-id
                                                 ::app.spec/host-id peer-id})))
                  (when-not replay?
                    (let [tournament|| (get @tournaments* frequency)]
                      (<! (app.tournament.chan/op
                           {::op.spec/op-key ::app.tournament.chan/create-tournament
                            ::op.spec/op-type ::op.spec/request-response
                            ::op.spec/op-orient ::op.spec/request}
                           tournament||
                           {::app.spec/frequency frequency
                            ::app.spec/peer-name peer-name
                            ::app.spec/peer-id peer-id
                            ::app.spec/host-id peer-id}))
                      (<! (app.tournament.chan/op
                           {::op.spec/op-key ::app.tournament.chan/join-tournament
                            ::op.spec/op-type ::op.spec/request-response
                            ::op.spec/op-orient ::op.spec/request}
                           tournament||
                           {::app.spec/frequency frequency
                            ::app.spec/peer-name peer-name
                            ::app.spec/peer-id peer-id
                            ::app.spec/host-id peer-id})))))
                (create-tournament-meta {::app.spec/frequency frequency
                                         ::app.spec/host-id peer-id})
                (when-not replay?
                  (<p! (.add @app-eventlog*
                             (pr-str (merge
                                      {::op.spec/op-key ::app.tournament.chan/create-tournament
                                       ::op.spec/op-type ::op.spec/request-response
                                       ::op.spec/op-orient ::op.spec/request}
                                      {::app.spec/frequency frequency
                                       ::app.spec/peer-name peer-name
                                       ::app.spec/peer-id peer-id
                                       ::app.spec/host-id host-id}))))
                  (<p! (.add @app-eventlog*
                             (pr-str (merge
                                      {::op.spec/op-key ::app.tournament.chan/join-tournament
                                       ::op.spec/op-type ::op.spec/request-response
                                       ::op.spec/op-orient ::op.spec/request}
                                      {::app.spec/frequency frequency
                                       ::app.spec/peer-name peer-name
                                       ::app.spec/peer-id peer-id
                                       ::app.spec/host-id host-id})))))
                (close! out|))


              {::op.spec/op-key ::app.tournament.chan/join-tournament
               ::op.spec/op-type ::op.spec/request-response
               ::op.spec/op-orient ::op.spec/request}
              (let [own-peer-id (get @state* ::app.spec/peer-id)
                    {:keys [::app.spec/peer-name
                            ::app.spec/frequency
                            ::app.spec/peer-id
                            ::replay?
                            ::op.spec/out|]} value]
                (when (not (get @tournaments* frequency))
                  (when (= own-peer-id peer-id)
                    (<! (create-tournament-proc {::app.spec/frequency frequency
                                                 ::app.spec/peer-name peer-name
                                                 ::app.spec/peer-id peer-id})))
                  (when-not replay?
                    (let [tournament|| (get @tournaments* frequency)]
                      (<! (app.tournament.chan/op
                           {::op.spec/op-key ::app.tournament.chan/join-tournament
                            ::op.spec/op-type ::op.spec/request-response
                            ::op.spec/op-orient ::op.spec/request}
                           tournament||
                           {::app.spec/frequency frequency
                            ::app.spec/peer-name peer-name
                            ::app.spec/peer-id peer-id})))))
                (when-not replay?
                  (<p! (.add @app-eventlog*
                             (pr-str (merge
                                      {::op.spec/op-key ::app.tournament.chan/join-tournament
                                       ::op.spec/op-type ::op.spec/request-response
                                       ::op.spec/op-orient ::op.spec/request}
                                      {::app.spec/frequency frequency
                                       ::app.spec/peer-name peer-name
                                       ::app.spec/peer-id peer-id})))))
                (close! out|))

              {::op.spec/op-key ::app.tournament.chan/leave-tournament
               ::op.spec/op-type ::op.spec/request-response
               ::op.spec/op-orient ::op.spec/request}
              (let [own-peer-id (get @state* ::app.spec/peer-id)
                    {:keys [::app.spec/peer-name
                            ::app.spec/frequency
                            ::app.spec/peer-id
                            ::replay?
                            ::op.spec/out|]} value]
                (when (get @tournaments* frequency)
                  (when-not replay?
                    (let [tournament|| (get @tournaments* frequency)]
                      (<! (app.tournament.chan/op
                           {::op.spec/op-key ::app.tournament.chan/leave-tournament
                            ::op.spec/op-type ::op.spec/request-response
                            ::op.spec/op-orient ::op.spec/request}
                           tournament||
                           {::app.spec/frequency frequency
                            ::app.spec/peer-id peer-id}))))
                  (when (= own-peer-id peer-id)
                    (<! (close-tournament-proc {::app.spec/frequency frequency
                                                ::app.spec/peer-id peer-id}))))
                (when-not replay?
                  (<p! (.add @app-eventlog*
                             (pr-str (merge
                                      {::op.spec/op-key ::app.tournament.chan/leave-tournament
                                       ::op.spec/op-type ::op.spec/request-response
                                       ::op.spec/op-orient ::op.spec/request}
                                      {::app.spec/frequency frequency
                                       ::app.spec/peer-name peer-name
                                       ::app.spec/peer-id peer-id})))))
                (close! out|))


              {::op.spec/op-key ::app.tournament.chan/close-tournament
               ::op.spec/op-type ::op.spec/request-response
               ::op.spec/op-orient ::op.spec/request}
              (let [{:keys [::app.spec/frequency
                            ::app.spec/peer-id
                            ::replay?
                            ::op.spec/out|]} value]
                (when (get @tournaments* frequency)
                  (when-not replay?
                    (let [tournament|| (get @tournaments* frequency)]
                      (<! (app.tournament.chan/op
                           {::op.spec/op-key ::app.tournament.chan/close-tournament
                            ::op.spec/op-type ::op.spec/request-response
                            ::op.spec/op-orient ::op.spec/request}
                           tournament||
                           {::app.spec/frequency frequency
                            ::app.spec/peer-id peer-id}))))
                  (<! (close-tournament-proc {::app.spec/frequency frequency
                                              ::app.spec/peer-id peer-id}))
                  (remove-tournament-meta {::app.spec/frequency frequency
                                           ::app.spec/peer-id peer-id}))
                (when-not replay?
                  (<p! (.add @app-eventlog*
                             (pr-str (merge
                                      {::op.spec/op-key ::app.tournament.chan/close-tournament
                                       ::op.spec/op-type ::op.spec/request-response
                                       ::op.spec/op-orient ::op.spec/request}
                                      {::app.spec/frequency frequency
                                       ::app.spec/peer-id peer-id})))))
                (close! out|))


              {::op.spec/op-key ::app.chan/request-tournament-stream
               ::op.spec/op-type ::op.spec/request-stream
               ::op.spec/op-orient ::op.spec/request}
              (let [{:keys [::app.spec/frequency
                            ::op.spec/out|]} value]
                (go (loop [counter 0]
                      (if-not (closed? out|)
                        (do
                          (put! out| counter)
                          (<! (timeout 1000))
                          (recur (inc counter)))
                        (do
                          (println ::stream-complete))))))))
          (recur))))))


(defonce ops (create-proc-ops channels ctx {}))

(defn main [& args]
  (println ::main)
  (app.chan/op
   {::op.spec/op-key ::app.chan/init
    ::op.spec/op-type ::op.spec/fire-and-forget}
   channels
   {}))

(def exports #js {:main main})

(when (exists? js/module)
  (set! js/module.exports exports))


(comment

  (cljc.core/rand-uuid)

  (go
    (println (<! (peernode.chan/op
                  {::op.spec/op-key ::peernode.chan/id
                   ::op.spec/op-type ::op.spec/request-response
                   ::op.spec/op-orient ::op.spec/request}
                  channels))))

  (go
    (let [out| (chan 64)]
      (peernode.chan/op
       {::op.spec/op-key ::peernode.chan/request-pubsub-stream
        ::op.spec/op-type ::op.spec/request-stream
        ::op.spec/op-orient ::op.spec/request}
       channels
       out|)
      (loop []
        (when-let [value  (<! out|)]
          (println value)
          (recur)))))


  (def counter (atom 0))

  (do
    (swap! counter inc)
    (peernode.chan/op
     {::op.spec/op-key ::peernode.chan/pubsub-publish
      ::op.spec/op-type ::op.spec/fire-and-forget}
     channels
     {::some @counter}))

  ;;
  )


(defn create-puppeteer
  []
  (go
    (try
      (let [data (<p! (.request axios
                                (clj->js
                                 {"url" "http://puppeteer:9222/json/version"
                                  "method" "get"
                                  "headers" {"Host" "localhost:9222"}})))

            webSocketDebuggerUrl (-> (aget (.-data data) "webSocketDebuggerUrl")
                                     (str/replace "localhost" "puppeteer"))]
        (<p! (.connect puppeteer
                       (clj->js
                        {"browserWSEndpoint" webSocketDebuggerUrl
                         #_"browserURL" #_"http://puppeteer:9222"}))))
      (catch js/Error err (println err)))))

(comment

  (go
    (let [data (<p! (.request axios
                              (clj->js
                               {"url" "http://puppeteer:9222/json/version"
                                "method" "get"
                                "headers" {"Host" "localhost:9222"}})))

          webSocketDebuggerUrl (-> (aget (.-data data) "webSocketDebuggerUrl")
                                   (str/replace "localhost" "puppeteer"))]
      (println webSocketDebuggerUrl)
      #_(println (js-keys data))
      #_(println (aget (.-data data) "webSocketDebuggerUrl"))))


  (go
    (try
      (let [page (<p! (.newPage browser))
            _ (<p! (.goto page "https://example.com"))
            dimensions (<p! (.evaluate page (fn []
                                              #js {"width" js/document.documentElement.clientWidth})))]
        (println dimensions))
      (catch js/Error err (println err))))

  ;;
  )