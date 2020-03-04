(ns common.alpha.game
  (:require [common.alpha.spec]
            [clojure.spec.alpha :as s]))

(defn gen-default-game-state
  [k ev]
  (let [host-uuid (:u/uuid ev)]
    {:g/uuid k
     :g/status :created
     :g/start-inst (java.util.Date.)
     :g/duration-ms 60000
     :g/map-size [16 16]
     :g/roles {host-uuid {:g.r/host true
                          :g.r/player 0
                          :g.r/observer false}}
     :g/player-states {0 {:g.p/entities {:g.p/cape {:g.e/type :g.e.type/cape
                                                    :g.e/uuid (java.util.UUID/randomUUID)
                                                    :g.e/pos [0 0]}}
                          :g.p/sum 0}
                       1 {:g.p/entities {:g.p/cape {:g.e/type :g.e.type/cape
                                                    :g.e/uuid (java.util.UUID/randomUUID)
                                                    :g.e/pos [0 15]}}
                          :g.p/sum 0}}
     :g/exit-teleports [{:g.e/type :g.e.type/teleport
                         :g.e/uuid (java.util.UUID/randomUUID)
                         :g.e/pos [15 0]}
                        {:g.e/type :g.e.type/teleport
                         :g.e/uuid (java.util.UUID/randomUUID)
                         :g.e/pos [15 15]}]
     :g/value-tiles (-> (mapcat (fn [x]
                                  (mapv (fn [y]
                                          {:g.e/uuid (java.util.UUID/randomUUID)
                                           :g.e/type :g.e.type/value-tile
                                           :g.e/pos [x y]
                                           :g.e/numeric-value (inc (rand-int 10))}) (range 0 16)))
                                (range 0 16))
                        (vec))}))

(s/fdef gen-default-game-state
  :args (s/cat :k uuid?
               :ev :ev.g/event)
  :ret :g/game)