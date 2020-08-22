(ns deathstar.gui.main
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [goog.string :refer [format]]
   [cljs.reader :refer [read-string]]
   [clojure.pprint :refer [pprint]]
   [reagent.core :as r]
   [reagent.dom :as rdom]

   [cljctools.vscode.tab-conn :as tab-conn.api]
   [cljctools.vscode.spec :as host.spec]
   [deathstar.gui.spec :as spec]
   [deathstar.gui.ops :as ops.api]
   [deathstar.gui.render :as render.api]))

(def channels (merge
               {::main| (chan 10)}
               (tab-conn.api/create-channels)
               (ops.api/create-channels)))

(def conn (tab-conn.api/create-proc-conn
           (merge
            {::host.spec/recv|  (::spec/ops| channels)
             ::host.spec/recv|m (::spec/ops|m channels)}
            channels)
           {}))

(def state (render.api/create-state))

(defn TMP-counter-inc
  []
  (swap! state update :counter inc))

(def ops (ops.api/create-proc-ops channels {:state state}))

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
              (let []
                (println ::main| ::start)
                (render.api/render-ui channels {:state state})))))
        (recur)))))

(def proc-main (create-proc-main channels {}))

(defn ^:export main
  []
  (println ::main)
  (put! (::main| channels) {:op ::start}))

(do (main))

