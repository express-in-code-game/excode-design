(ns common.alpha.game
  (:require
   [clojure.spec.alpha :as s]
   [clojure.test.check.generators :as gen]
   [common.alpha.time :as ct]))

(defn mk-default-game-state
  [k ev]
  (let [host-uuid (:u/uuid ev)]
    {:g/uuid k
     :g/status :created
     :g/start-inst #inst "2020-03-04T06:15:12.298-00:00"
     :g/duration-ms 60000
     :g/map-size [16 16]
     :g/roles {host-uuid {:g.r/host true
                          :g.r/player 0
                          :g.r/observer false}}
     :g/player-states {0 {:g.p/entities {:g.p/cape {:g.e/type :g.e.type/cape
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
                        (vec))}))

(s/fdef mk-default-game-state
  :args (s/cat :k uuid?
               :ev :ev.g/event)
  :ret :g/game)