(ns github.sergeiudris.rovers.scenario.impl
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.core.async.impl.protocols :refer [closed?]]
   #?(:cljs [cljs.core.async.interop :refer-macros [<p!]])
   #?(:cljs [goog.string.format])
   #?(:cljs [goog.string :refer [format]])
   #?(:cljs [goog.object])
   #?(:cljs [cljs.reader :refer [read-string]])

   [clojure.string :as string]
   [cljctools.csp.op.spec :as op.spec]
   [cljctools.cljc.core :as cljc.core]

   [github.sergeiudris.rovers.player.spec :as player.spec]
   [github.sergeiudris.rovers.player.chan :as player.chan]

   [github.sergeiudris.rovers.scenario.spec :as scenario.spec]
   [github.sergeiudris.rovers.scenario.chan :as scenario.chan]
   [github.sergeiudris.rovers.scenario.core :as scenario.core]))


(defn create-proc-ops
  [channels state* opts]
  (let [{:keys [::scenario.chan/ops|
                ::scenario.chan/exit|]} channels]
    (go

      (loop []
        (when-let [[value port] (alts! [ops|])]
          (condp = port

            exit|
            (let [{:keys [::op.spec/out|]} value]
              (close! out|))

            ops|
            (do
              (do :game-cycle-ops-like-updating-effects) 
              (condp = (select-keys value [::op.spec/op-key ::op.spec/op-type ::op.spec/op-orient])

                {::op.spec/op-key ::scenario.chan/move-rovers
                 ::op.spec/op-type ::op.spec/fire-and-forget}
                (let [{:keys [::scenario.spec/choose-location
                              ::scenario.spec/location-type
                              ::scenario.spec/x-offset
                              ::scenario.spec/y-offset]} value
                      {:keys [::scenario.spec/rovers]
                       :as state} @state*]
                  (cond
                    (= choose-location ::scenario.spec/closest)
                    (as-> nil result
                      (reduce
                       (fn [result [k-rover rover]]
                         (let [location (scenario.core/rover-closest-location state rover value)]
                           (-> result
                               (scenario.core/rover-visits-location rover location))))
                       state rovers)
                      (merge result (scenario.core/entities-in-range result))
                      (swap! state* merge result))))


                {::op.spec/op-key ::scenario.chan/scan
                 ::op.spec/op-type ::op.spec/fire-and-forget}
                (let [{:keys [::scenario.spec/energy-percentage]} value
                      {:keys [::scenario.spec/rovers]
                       :as state} @state*]
                  (println ::scenario.spec/energy-percentage energy-percentage)
                  (as-> nil result
                    (reduce
                     (fn [result [k-rover rover]]
                       (let []
                         (-> result
                             (scenario.core/rover-scans rover value))))
                     state rovers)
                    (swap! state* merge result))))
              (recur))
            ;
            ))))))