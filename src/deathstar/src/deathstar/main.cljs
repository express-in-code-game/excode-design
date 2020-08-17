(ns deathstar.main
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
   [deathstar.extension :as extension.api]
   #_[pad.cljsjs1]
   #_[pad.selfhost1]))

(def host|| (host.api/create-channels))

(defn main [] nil)

(def exports #js {:activate (fn [context]
                              (println "activating")
                              (host.api/activate host|| context))
                  :deactivate (fn []
                                (println "deactivating")
                                (host.api/deactivate host||))})
(when (exists? js/module)
  (set! js/module.exports exports))

(def extension|| (extension.api/create-channels))

; TBD where this belongs
(do
  (let [{:keys [extension-ops|x]} extension||
        {:keys [host-evt|m]} host||]
    (admix extension-ops|x (tap host-evt|m (chan 10 (comp (filter (every-pred (fn [v]
                                                                                (println "filtering")
                                                                                (#{:host/extension-activate :host/extension-deactivate} (:op v)))))))))))

(def host (host.api/create-proc-host host|| {}))

(def ops (extension.api/create-proc-ops (merge host|| extension||) {:host host}))

(def log (extension.api/create-proc-log (merge host|| extension||) {}))


(comment
  
  (reduce (fn [ag x] (assoc ag x x)) {} #{1 2 3})
  
  ;;
  )

#_(defn reload
    []
    (.log js/console "Reloading...")
    (js-delete js/require.cache (js/require.resolve "./main")))
