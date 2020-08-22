(ns deathstar.scenario.rovers1.game-api
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.set :refer [subset?]]
   [deathstar.scenario.rovers1.api :as api]
   ))


(def ^:private state (api/create-state (api/generate-state-data)))

(add-watch state :watcher
           (fn [key atom old-state new-state]
             (let [pos (:pos new-state)]
               (println (format "new position is %s" pos)))))

(def ^:private channels (api/create-channels))

(def ^:private input| (::api/input| channels))

(def ^:private scenario (api/create-proc-scenario channels {:state state}))

(def ^:private simulation (api/create-proc-simulation channels {:state state}))

(defn move
  [x y]
  (put! input| {:op ::api/move :pos {:x x :y y}}))

(defn scan
  []
  (put! input| {:op ::api/scan}))