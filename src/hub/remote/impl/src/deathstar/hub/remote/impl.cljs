(ns deathstar.hub.remote.impl
  #?(:cljs (:require-macros [deathstar.hub.remote.impl]))
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.spec.alpha :as s]
   [cljctools.cljc.core :as cljc.core]
   [cljctools.csp.op.spec :as op.spec]
   [deathstar.user.spec :as user.spec]
   [deathstar.game.spec :as game.spec]
   [deathstar.hub.chan :as hub.chan]
   [deathstar.hub.spec :as hub.spec]
   [deathstar.hub.remote.spec :as tap.remote.spec]))


(defn create-state
  []
  (atom {::user.spec/user nil
         ::user.spec/users {}
         ::game.spec/games {}}))

(defn create-proc-ops
  [channels state]
  (let [{:keys [::extension.chan/ops|
                ::http-chan.chan/request|
                ::host.chan/cmd|m
                ::host.chan/tab-evt|m]
         socket-evt|m ::socket.chan/evt|m
         host-evt|m ::host.chan/evt|m} channels
        cmd|t (tap cmd|m (chan 10))
        relevant-socket-evt? (fn [v]  (#{::socket.chan/connected ::socket.chan/closed} (::op.spec/op-key v)))
        socket-evt|t (tap socket-evt|m (chan 10 (comp (filter (every-pred relevant-socket-evt?)))))
        relevant-host-evt? (fn [v]  (#{::host.chan/extension-activate ::host.chan/extension-deactivate} (::op.spec/op-key v)))
        host-evt|t (tap host-evt|m (chan 10 (comp (filter (every-pred relevant-host-evt?)))))
        relevant-tab-evt? (fn [v]  (#{::host.chan/tab-disposed} (::op.spec/op-key v)))
        tab-evt|t (tap tab-evt|m (chan 10 (comp (filter (every-pred relevant-tab-evt?)))))]
    (go
      (loop []
        (when-let [[v port] (alts! [ops| host-evt|t cmd|t socket-evt|t])]
          (do (println ::value v))
          (condp = port
            host-evt|t
            (condp = (select-keys v [::op.spec/op-key ::op.spec/op-type])

              {::op.spec/op-key ::host.chan/extension-activate
               ::op.spec/op-type ::op.spec/request}
              (let [workspaceFolder (<! (host.impl/select-workspaceFolder {}))
                    deathstar-edn (as-> nil x
                                    (<! (host.impl/read-workspaceFolder-file
                                         workspaceFolder
                                         "deathstar.edn"))
                                    (when x
                                      (->> x
                                           (.toString)
                                           (read-string)
                                           (apply merge))))]
                (when-not deathstar-edn
                  (host.chan/op
                   {::op.spec/op-key ::host.chan/show-info-msg}
                   channels
                   "workspace contains no deathstar.edn"))

                (when deathstar-edn
                  (do (set! *workspaceFolder* workspaceFolder))
                  (println ::extension-activate)
                  (println deathstar-edn)
                  (swap! state merge deathstar-edn)
                  (let []
                    (host.chan/op
                     {::op.spec/op-key ::host.chan/show-info-msg}
                     channels
                     "Death Star activating")
                    (socket.chan/op
                     {::op.spec/op-key ::socket.chan/connect}
                     channels
                     {::socket.spec/url (state->socket-url @state)})
                    (host.chan/op
                     {::op.spec/op-key ::host.chan/cmd}
                     (::host.chan/cmd| channels)
                     "deathstar.open")))))

            socket-evt|t
            (condp = (select-keys v [::op.spec/op-key ::op.spec/op-type])

              {::op.spec/op-key ::socket.chan/connected}
              (let []
                (println ::socket-connected)
                (hub.chan/op
                 {::op.spec/op-key ::hub.chan/user-join
                  ::op.spec/op-type ::op.spec/request}
                 channels
                 {::user.spec/uuid (cljc/rand-uuid)})
                (hub.chan/op
                 {::op.spec/op-key ::hub.chan/list-users
                  ::op.spec/op-type ::op.spec/request}
                 channels))

              {::op.spec/op-key ::socket.chan/closed}
              (let []
                (println ::socket-closed)))

            ops|
            (condp = (select-keys v [::op.spec/op-key ::op.spec/op-type])

              {::op.spec/op-key ::extension.chan/update-settings-filepaths
               ::op.spec/op-type ::op.spec/request}
              (let [{:keys [::extension.spec/deathstar-dir ::op.spec/out|]} @state
                    {:as resp :keys [::host.spec/filenames]} (<! (host.chan/op
                                                                  {::op.spec/op-key ::host.chan/read-dir
                                                                   ::op.spec/op-type ::op.spec/request}
                                                                  channels deathstar-dir))]
                (swap! state assoc ::extension.spec/settings-filepaths filenames)
                (extension.chan/op
                 {::op.spec/op-key ::extension.chan/update-settings-filepaths
                  ::op.spec/op-type ::op.spec/response}
                 out| filenames))

              {::op.spec/op-key ::extension.chan/apply-settings-file
               ::op.spec/op-type ::op.spec/request}
              (let [{:keys [::extension.spec/filepath ::op.spec/out|]} v
                    {:as resp :keys [::host.spec/file-content]} (<! (host.chan/op
                                                                     {::op.spec/op-key ::extension.chan/read-file
                                                                      ::op.spec/op-type ::op.spec/request}
                                                                     channels filepath))
                    settings (read-string file-content)]
                #_(swap! state merge (apply merge settings))
                #_(<! (ws.api/disconnect net-ws))
                #_(<! (remote.api/disconnect remote))
                #_(<! (ws.api/connect net-ws))
                #_(<! (remote.api/connect remote))
                (extension.chan/op
                 {::op.spec/op-key ::extension.chan/apply-settings-file
                  ::op.spec/op-type ::op.spec/response}
                 out| settings)))

            tab-evt|t
            {::op.spec/op-key ::host.chan/tab-disposed}
            (let []
              (println ::tab-disposed)
              (swap! state dissoc ::gui-tab))

            cmd|t
            (condp = (::host.spec/cmd-id v)

              (extension.spec/assert-cmd-id "deathstar.open")
              (cond

                (get @state ::gui-tab)
                (host.chan/op
                 {::op.spec/op-key ::host.chan/show-info-msg}
                 channels
                 "deathstar is already open")

                (not (get @state ::gui-tab))
                (let [tab-create-opts {::host.spec/tab-id "gui-tab"
                                       ::host.spec/tab-title "Death Star"
                                       ::host.spec/tab-html-replacements
                                       {"./out/deathstar-extension-gui/main.js" "resources/out/deathstar-extension-gui/main.js"
                                        "./css/style.css" "resources/antd.min-4.6.1.css"}
                                       ::host.spec/tab-html-filepath "resources/extension-gui.html"}]
                  (host.chan/op
                   {::op.spec/op-key ::host.chan/tab-create}
                   channels
                   tab-create-opts)
                  (host.chan/op
                   {::op.spec/op-key ::host.chan/show-info-msg}
                   channels
                   "deathstar opening")
                  (swap! state assoc ::gui-tab tab-create-opts)))

              (extension.spec/assert-cmd-id "deathstar.ping")
              (let [tab (get @state ::gui-tab)]
                (extension.gui.chan/op
                 {::op.spec/op-key ::extension.gui.chan/update-state}
                 channels
                 @state
                 (select-keys tab [::host.spec/tab-id]))
                #_(host.chan/op
                   {::op.spec/op-key ::host.chan/tab-send}
                   channels
                   {::some-value-value-to-send nil}))
              (host.chan/op
               {::op.spec/op-key ::host.chan/show-info-msg}
               channels
               "deathstar.ping"))))
        (recur))
      (println "; proc-ops go-block exiting"))))


#_(defn toggle-loading
    ([state op-key]
     (toggle-loading state op-key (not (get-in @state [op-key ::tap.remote.spec/loading?]))))
    ([state op-key loading?]
     (swap! state update op-key assoc ::tap.remote.spec/loading?  loading?)))

(defn proc-loading
  [ops| state]
  (go
    (loop []
      (when-let [[v port] (alts! [ops|])]
        (let [{:keys [::op.spec/op-key ::op.spec/op-type]} v]
          (condp = op-type

            ::op.spec/request
            (swap! state update op-key assoc ::loading?  true)

            ::op.spec/response
            (swap! state update op-key assoc ::loading?  false)

            (do nil))))
      (recur))))