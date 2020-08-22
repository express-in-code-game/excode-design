(ns deathstar.gui.ops
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

   [deathstar.core.protocols :as p]
   [deathstar.core.spec :as core.spec]
   [cljctools.vscode.spec :as host.spec]
   [deathstar.gui.spec :as spec]))

(defn create-channels
  []
  (let [ops| (chan 10)
        ops|m (mult ops|)]
    {::spec/ops| ops|
     ::spec/ops|m ops|m}))

(defn create-proc-ops
  [channels ctx]
  (let [{:keys [::spec/ops|m]} channels
        ; recv|t (tap recv|m (chan 10))
        ops|t (tap ops|m (chan 10))
        {:keys [state]} ctx]
    (.addEventListener js/document "keyup"
                       (fn [ev]
                         (cond
                           (and (= ev.keyCode 76) ev.ctrlKey) (swap! state assoc :data []))))
    (go
      (loop []
        (when-let [[v port] (alts! [ops|t])]
          (condp = port
            ops|t (condp = (:op v)

                    (core.spec/op
                     ::core.spec/gui-ops|
                     ::core.spec/update-gui-state)
                    (let []
                      (println ::core.spec/update-gui-state)
                      (prn v))

                    :some-op
                    (let [{:keys [data]} v]
                      (swap! state update :data conj data)))))
        (recur))
      (println "; proc-ops go-block exiting"))))





