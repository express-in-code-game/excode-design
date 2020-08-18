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

   [cljctools.vscode.api :as host.api]
   [deathstar.extension.app :as app.api]
   [cljctools.net.socket.api :as ws.api]
   #_[pad.cljsjs1]
   #_[pad.selfhost1]))

(def host|| (host.api/create-channels))

(defn ^:export main []
  (println "deathstar.extension.main"))

(def exports #js {:activate (fn [context]
                              (println "activating")
                              (host.api/activate host|| context))
                  :deactivate (fn []
                                (println "deactivating")
                                (host.api/deactivate host||))})
(when (exists? js/module)
  (set! js/module.exports exports))

(def extension|| (app.api/create-channels))

#_(do
    (let [{:keys [ext-ops|x]} extension||
          {:keys [host-evt|m]} host||]
      (admix ext-ops|x (tap host-evt|m (chan 10 (comp (filter (every-pred (fn [v]
                                                                                  (println "filtering")
                                                                                  (#{:host/extension-activate :host/extension-deactivate} (:op v)))))))))))

(def state (atom {:solution-space-tab nil}))

(def host (host.api/create-proc-host host|| {}))

(def ops (app.api/create-proc-ops (merge host|| extension||) {:host host
                                                                    :state state}))

(def log (app.api/create-proc-log (merge host|| extension||) {}))

(def net-ws|| (ws.api/create-channels))

(comment

  (reduce (fn [ag x] (assoc ag x x)) {} #{1 2 3})



  (def net-ws (ws.api/create-proc-ws net-ws|| {} {:url "ws://localhost:8081/ws"}))

  (ws.api/send-data net-ws {:a 1})
  (ws.api/connected? net-ws)

  ;;
  )

#_(defn reload
    []
    (.log js/console "Reloading...")
    (js-delete js/require.cache (js/require.resolve "./main")))
