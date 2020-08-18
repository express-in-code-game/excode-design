(ns deathstar.tabapp.solution-space.app
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

   [deathstar.protocols :as p]
   [deathstar.spec :as sp]))

(defn create-channels
  []
  (let [ops| (chan 10)
        ops|m (mult ops|)]
    {:ops| ops|
     :ops|m ops|m}))

(defn create-proc-ops
  [channels ctx]
  (let [{:keys [ops|m conn-recv|m]} channels
        conn-recv|t (tap conn-recv|m (chan 10))
        ops|t (tap ops|m (chan 10))
        {:keys [state]} ctx]
    (.addEventListener js/document "keyup"
                       (fn [ev]
                         (cond
                           (and (= ev.keyCode 76) ev.ctrlKey) (swap! state assoc :data []))))
    (go
      (loop []
        (when-let [[v port] (alts! [ops|t conn-recv|t])]
          (condp = port
            ops|t (condp = (:op v)
                    :solution-space/append (let [{:keys [data]} v]
                                             (swap! state update :data conj data)))
            conn-recv|t (condp = (:op v)
                          (sp/op :solution-tab| :solution-space/eval) (do
                                                                        (println "eval string:")
                                                                        (prn v)))))
        (recur))
      (println "; proc-ops go-block exiting"))))





