(ns deathstar.scenario.rovers1.api
  #?(:cljs (:require-macros [deathstar.scenario.rovers1.api]))
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.set :refer [subset?]]
   [clojure.spec.alpha :as s]
   [clojure.test.check.generators :as gen]

   [deathstar.core.protocols :as p]))

(s/def ::x int?)
(s/def ::y int?)
(s/def ::pos (s/keys :req-un [::x ::y]))

(s/def ::state (s/keys :req-un [::pos]))

(defn create-state
  [data]
  (atom data))

(defmulti swap-state
  {:arglists '([state value])}
  (fn [state value] (:op value)))

(defmethod swap-state ::move
  [state v]
  (let [{:keys [pos]} v]
    (swap! state assoc :pos pos)))

; https://github.com/sergeiudris/lab.cloud-200427/blob/5ad474218a76d2f45fb09f069b8f189dd198cf53/common/test/starnet/common/pad/spec1.cljc
; https://github.com/sergeiudris/lab.cloud-200427/blob/5ad474218a76d2f45fb09f069b8f189dd198cf53/common/test/starnet/common/pad/spec2.cljc

(defn generate-state-data
  []
  {:pos {:x (gen/generate gen/small-integer)
         :y (gen/generate (gen/large-integer* {:min 0 :max 63}))}})

(defn create-channels
  []
  (let [input| (chan 10)
        input|m (mult input|)]
    {::input| input|
     ::input|m input|m}))

(defn create-proc-scenario
  [channels ctx]
  (let []
    (go (loop []))
    (reify
      p/Scenario
      (-foo [_])
      p/Release
      (-release [_]))))

(defn create-proc-simulation
  [channels ctx]
  (let [{:keys [::input| ::input|m]} channels
        input|t (tap input|m (chan 10))
        state (:state ctx)]
    (go (loop []
          (when-let [v (<! input|t)]
            (condp = (:op v)
              ::move
              (swap-state state v)))
          (recur)))
    (reify
      p/Simulation
      (-bar [_])
      p/Release
      (-release [_]))))

(defn release
  [_]
  (p/-release _))