(ns deathstar.extension.gui.main
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [goog.string :refer [format]]
   [cljs.reader :refer [read-string]]
   [clojure.pprint :refer [pprint]]

   [cljctools.vscode.tab-conn.impl :as tab-conn.impl]
   [cljctools.vscode.tab-conn.chan :as tab-conn.chan]

   [cljctools.csp.op.spec :as op.spec]

   [deathstar.extension.spec :as extension.spec]
   [deathstar.extension.chan :as extension.chan]
   
   [deathstar.extension.gui.chan :as extension.gui.chan]
   [deathstar.extension.gui.render :as extension.gui.render]))


(def channels (let [chs (merge
                         (extension.chan/create-channels)
                         (extension.gui.chan/create-channels)
                         (tab-conn.chan/create-channels))]
                (merge chs {::tab-conn.chan/recv|  (::extension.gui.chan/ops| chs)
                            ::tab-conn.chan/recv|m (::extension.gui.chan/ops|m chs)})))

(def state (extension.gui.render/create-state {}))

(def tab-conn (tab-conn.impl/create-proc-conn channels state))


(defn create-proc-ops
  [channels state]
  (let [{:keys [::extension.gui.chan/ops|m]} channels
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
            ops|t
            (condp = (select-keys v [::op.spec/op-key ::op.spec/op-type])

              {::op.spec/op-key ::extension.gui.chan/init}
              (let []
                (extension.gui.render/render-ui channels state {}))

              {::op.spec/op-key ::extension.gui.chan/update-state}
              (let [{state* ::extension.spec/state} v]
                (reset! state state*)))))
        (recur))
      (println (format "go-block exit %s" ::create-proc-ops)))))


(def proc-ops (create-proc-ops channels state))

(defn ^:export main
  []
  (println ::main)
  (extension.gui.chan/op
   {::op.spec/op-key ::extension.gui.chan/init}
   channels))

(do (main))

