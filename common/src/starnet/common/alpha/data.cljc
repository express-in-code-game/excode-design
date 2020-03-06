(ns starnet.common.alpha.data
  (:require
   [clojure.spec.alpha :as s]
   [clojure.test.check.generators :as gen]
   [starnet.common.alpha.core :refer [make-inst]]))

(defn make-game-state
  [k ev]
  {:g/player-states {0 {:g.p/entities {:g.p/cape {:g.e/type :g.e.type/cape
                                                  :g.e/uuid (gen/generate gen/uuid)
                                                  :g.e/pos [0 0]}}
                        :g.p/sum 0}
                     1 {:g.p/entities {:g.p/cape {:g.e/type :g.e.type/cape
                                                  :g.e/uuid (gen/generate gen/uuid)
                                                  :g.e/pos [0 15]}}
                        :g.p/sum 0}}
   :g/exit-teleports [{:g.e/type :g.e.type/teleport
                       :g.e/uuid (gen/generate gen/uuid)
                       :g.e/pos [15 0]}
                      {:g.e/type :g.e.type/teleport
                       :g.e/uuid (gen/generate gen/uuid)
                       :g.e/pos [15 15]}]
   :g/value-tiles (-> (mapcat (fn [x]
                                (mapv (fn [y]
                                        {:g.e/uuid (gen/generate gen/uuid)
                                         :g.e/type :g.e.type/value-tile
                                         :g.e/pos [x y]
                                         :g.e/numeric-value (inc (rand-int 10))}) (range 0 1)))
                              (range 0 1))
                      (vec))
   :g/map-size [16 16]})

(defn make-game
  [k ev]
  (let [host-uuid (:u/uuid ev)]
    {:g/uuid k
     :g/status :created
     :g/start-inst (make-inst)
     :g/duration-ms 60000
     :g/roles {host-uuid {:g.r/host true
                          :g.r/player nil
                          :g.r/observer false}}
     :g/state (make-game-state k ev)}))


(defn next-game-state
  [state ev]
  state)

