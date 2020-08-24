(ns deathstar.extension.gui.main
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [goog.string :refer [format]]
   [cljs.reader :refer [read-string]]
   [clojure.pprint :refer [pprint]]

   [cljctools.vscode.tab-conn :as tab-conn.api]
   [cljctools.vscode.spec :as host.spec]
   [deathstar.gui.ops :as ops.api]
   [deathstar.gui.render :as render.api]

   [deathstar.extension.spec :as spec]))

(declare )

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

(def state (render.api/create-state {}))

(def ops (ops.api/create-proc-ops channels state))


#_(defn create-state
    [data]
    (atom data))

#_(defn create-default-state-data
    []
    {:data []
     :counter 0})

(defn create-channels
  []
  (let [ops| (chan 10)
        ops|m (mult ops|)]
    {::spec/ops| ops|
     ::spec/ops|m ops|m}))

(defn create-proc-ops
  [channels state]
  (let [{:keys [::spec/ops|m]} channels
        ; recv|t (tap recv|m (chan 10))
        ops|t (tap ops|m (chan 10))]
    (.addEventListener js/document "keyup"
                       (fn [ev]
                         (cond
                           (and (= ev.keyCode 76) ev.ctrlKey) (println ::ctrl+l) #_(swap! state assoc :data []))))
    (go
      (loop []
        (when-let [[v port] (alts! [ops|t])]
          (condp = port
            ops|t (condp = (:op v)

                    (spec/op
                     ::spec/ops|
                     ::spec/update-state)
                    (let [{:keys [data]}]
                      (println ::spec/update-state)
                      (reset! state data)))))
        (recur))
      (println (format "go-block exit %s" ::create-proc-ops)))))



(defn create-proc-main
  [channels state]
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

