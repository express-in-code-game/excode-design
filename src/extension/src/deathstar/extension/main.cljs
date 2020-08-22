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
   [deathstar.extension.ops :as ops.api]
   [deathstar.extension.spec :as spec]
   [cljctools.net.socket.api :as ws.api]
   [deathstar.extension.http-chan :as http-chan.api]
   #_[pad.cljsjs1]
   #_[pad.selfhost1]))

(def channels (merge
               (host.api/create-channels)
               (ops.api/create-channels)
               (ws.api/create-channels)
               (http-chan.api/create-channels)))

(defn ^:export main []
  (println "deathstar.extension.main"))

(def exports #js {:activate (fn [context]
                              (println "activating")
                              (host.api/activate channels context))
                  :deactivate (fn []
                                (println "deactivating")
                                (host.api/deactivate channels))})
(when (exists? js/module)
  (set! js/module.exports exports))


#_(do
    (let [{:keys [ext-ops|x]} extension||
          {:keys [host-evt|m]} channels]
      (admix ext-ops|x (tap host-evt|m (chan 10 (comp (filter (every-pred (fn [v]
                                                                                  (println "filtering")
                                                                                  (#{:host/extension-activate :host/extension-deactivate} (:op v)))))))))))

(def state (atom {::spec/gui-tab nil}))

(def host (host.api/create-proc-host channels {}))


(def ops (ops.api/create-proc-ops
          (merge
           {:http| (::http-chan.api/http| channels)}
           channels)
          {:host host
           :state state}))

(def log (ops.api/create-proc-log channels {}))

(def http-chan (http-chan.api/create-proc-ops channels {} {::http-chan.api/url "http://localhost:8080/api"}))

#_(def net-ws (ws.api/create-proc-ws channels {} {:url "ws://localhost:8081/ws"}))

(comment

  (reduce (fn [ag x] (assoc ag x x)) {} #{1 2 3})

  (ws.api/send-data net-ws {:a 1})
  (ws.api/connected? net-ws)

  ;;
  )

#_(defn reload
    []
    (.log js/console "Reloading...")
    (js-delete js/require.cache (js/require.resolve "./main")))
