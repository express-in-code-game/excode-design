(ns deathstar.extension.main
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [goog.string :refer [format]]
   [clojure.string :as string]
   [cljs.reader :refer [read-string]]
   [clojure.pprint :refer [pprint]]

   [cljctools.csp.spec ::as csp.spec]
   [cljctools.vscode.api :as host.api]
   [deathstar.extension.spec :as spec]
   [deathstar.multiplayer.spec :as multiplayer.spec]
   [cljctools.net.socket.api :as ws.api]
   [deathstar.extension.http-chan :as http-chan.api]
   [deathstar.multiplayer.remote.api :as remote.api]


   [cljctools.csp.spec ::as csp.spec]

   [cljctools.self-hosted.compiler :as compiler.api]
   [cljctools.self-hosted.api :as self-hosted.api]

   [cljctools.vscode.spec :as host.spec]
   [cljctools.vscode.api :as host.api]

   [cljctools.net.core.spec :as net.spec]
   [deathstar.extension.spec :as extension.spec]
   [deathstar.core.spec :as core.spec]
   #_[pad.cljsjs1]
   #_[pad.selfhost1]))

(def state (atom {::spec/settings (apply merge
                                         [#:deathstar.extension.spec{:nrepl-port 7071
                                                                     :server-port 8080
                                                                     :server-host "localhost"
                                                                     :http-path "/api"}
                                          #:deathstar.multiplayer.spec{:username "Player 1"}])
                  ::settings-filepaths []
                  ::remote.spec/status nil
                  ::spec/gui-tab nil}))

(def channels (merge
               (host.api/create-channels)
               (ops.api/create-channels)
               (ws.api/create-channels)
               (http-chan.api/create-channels)
               (remote.api/create-channels)))

#_(defn ^:export main [& args]
    (println ::-main))

(def exports #js {:activate (fn [context]
                              (println ::activate)
                              (host.api/activate channels context))
                  :deactivate (fn []
                                (println ::deactivate)
                                (host.api/deactivate channels context))})
(when (exists? js/module)
  (set! js/module.exports exports))

#_(defn reload
    []
    (.log js/console "Reloading...")
    (js-delete js/require.cache (js/require.resolve "./main")))

(def api (let [api (atom {})
               host (host.api/create-proc-host channels nil nil)
               log (ops.api/create-proc-log channels state api)
               http-chan (http-chan.api/create-proc-ops channels state api)
               remote (remote.api/create-proc-ops (merge
                                                   {}
                                                   channels) state api)]
           (reset! api
                   {::host.api/host host
                    ::http-chan.api/http-chan http-chan
                    ::remote.api/remote remote})))

(def host (host.api/create-proc-host channels {}))


#_(def ops (ops.api/create-proc-ops
          (merge
           {:http| (::http-chan.api/http| channels)}
           channels)
          state))

#_(def log (ops.api/create-proc-log channels {}))

(def http-chan (http-chan.api/create-proc-ops channels state))

(def remote (remote.api/create-proc-ops (merge
                                         {}
                                         channels) state))

(def net-ws (ws.api/create-proc-ws channels state))

(comment

  (reduce (fn [ag x] (assoc ag x x)) {} #{1 2 3})

  (ws.api/send-data net-ws {:a 1})
  (ws.api/connected? net-ws)

  ;;
  )

(defn create-channels
  []
  (let [extension-ops| (chan 10)
        extension-ops|m (mult extension-ops|)
        extension-ops|x (mix extension-ops|)
        cmd| (chan 10)
        cmd|m (mult cmd|)
        tab-state| (chan (sliding-buffer 10))
        tab-state|m (mult tab-state|)
        input| (chan 10)]
    {::extension.spec/cmd| cmd|
     ::extension.spec/cmd|m cmd|m
     ::extension.spec/tab-state| tab-state|
     ::extension.spec/tab-state|m tab-state|m
     ::extension.spec/input| input|

     ::core.spec/extension-ops| extension-ops|
     ::core.spec/extension-ops|m extension-ops|m}))


(defn create-proc-ops
  [channels state apis]
  (let [{:keys [::core.spec/extension-ops| ::core.spec/extension-ops|m
                :http| ::extension.spec/input| ::extension.spec/evt|
                ::extension.spec/cmd| ::extension.spec/cmd|m ::extension.spec/tab-state|]
         socket|m ::net.spec/recv|m
         host-evt|m ::host.spec/evt|m} channels
        extension-ops|t (tap extension-ops|m (chan 10))
        cmd|t (tap cmd|m (chan 10))
        socket|t (tap socket|m (chan 10))
        relevant-evt? (fn [v]  ((host.spec/ops #{::host.spec/extension-activate ::host.spec/extension-deactivate}) (::csp.spec/op v)))
        host-evt|t (tap host-evt|m (chan 10 (comp (filter (every-pred relevant-evt?)))))]
    (go
      (loop []
        (when-let [[v port] (alts! [extension-ops|t host-evt|t cmd|t])]
          (condp = port
            host-evt|t (condp = (::csp.spec/op v)

                         (host.spec/op
                          ::host.spec/evt|
                          ::host.spec/extension-activate)
                         (let []
                           (println ::host.spec/extension-activate)
                           (host.api/show-info-msg host "Death Star activating")
                           (host.api/register-commands host {::extension.spec/cmd-ids spec/cmd-ids
                                                             ::host.spec/cmd| cmd|})))

            extension-ops|t (condp = (::csp.spec/op v)
                              ::extension.spec/some-op (do nil))
            socket|t (let []
                       (println "data from socket" v))
            input| (condp = (::csp.spec/op v)

                     ::extension.spec/list-configs
                     (let [{:keys [::extension.spec/settings ::extension.spec/deathstar-dir]} @ctx
                           {:keys [out|]} v
                           filenames (<! (host.api/readdir deathstar-dir))
                           resp (merge v {::csp.spec/op-status ::csp.spec/complete
                                          ::extension.spec/settings-filepaths filenames})]
                       (put! out| resp))

                     ::extension.spec/apply-config
                     (let [{:keys [::extension.spec/settings ::extension.spec/deathstar-dir]} @ctx
                           {:keys [filepath out|]} v
                           settings (<! (host.api/readfile filepath))
                           resp (merge v {::csp.spec/op-status ::csp.spec/complete
                                          ::extension.spec/settings settings})]
                       (println ::apply-config)
                       (swap! ctx assoc ::spec/settings (apply merge settings))
                       (<! (ws.api/disconnect net-ws))
                       (<! (remote.api/disconnect remote))
                       (<! (ws.api/connect net-ws))
                       (<! (remote.api/connect remote))
                       (put! out| resp)))

            cmd|t (condp = (::host.spec/cmd-id v)

                    (spec/cmd-id "deathstar.open")
                    (host.api/show-info-msg host "deathstar.open")

                    (spec/cmd-id "deathstar.ping")
                    (host.api/show-info-msg host "deathstar.ping")

                    (spec/cmd-id "deathstar.gui.open")
                    (let [tab (host.api/create-tab
                               host
                               {::host.spec/tab-id "gui-tab"
                                ::host.spec/tab-title "Death Star"
                                ::host.spec/tab-script-path "resources/out/deathstar-gui/main.js"
                                ::host.spec/tab-html-path "resources/gui.html"
                                ::host.spec/tab-script-replace "./out/deathstar-gui/main.js"
                                ::host.spec/tab-msg| extension-ops|
                                ::host.spec/tab-state| tab-state|})]
                      (swap! state assoc ::extension.spec/gui-tab tab))

                    (spec/cmd-id "deathstar.solution-tab-eval")
                    (let [tab (get @state ::extension.spec/gui-tab)]
                      (host.api/send-tab
                       tab
                       (core.spec/vl ::core.spec/gui-ops| {::csp.spec/op ::core.spec/update-gui-state
                                                           ::extension.spec/state nil}))))))
        (recur))
      (println "; proc-ops go-block exiting"))))


#_(defn create-proc-log
    [channels ctx]
    (let []
      (go (loop []
            (<! (chan 1))
            (recur))
          (println "; proc-log go-block exiting"))))


