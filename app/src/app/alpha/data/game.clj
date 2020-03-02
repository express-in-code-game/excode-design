(ns app.alpha.data.game
  (:require [clojure.repl :refer [doc]]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.spec.test.alpha :as stest]
            [app.alpha.spec :refer [gen-ev-p-move-cape
                                    gen-ev-a-finish-game]]))

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

(defmulti next-state
  "Returns the next state of the game."
  {:arglists '([state key event ])}
  (fn [state k ev] [(:ev/type ev)]))

(defmethod next-state [:ev.c/delete-record]
  [state k ev]
  nil)

(defmethod next-state [:ev.g.u/create]
  [state k ev]
  (gen-default-game-state k ev))

(defmethod next-state [:ev.g.u/delete]
  [state k ev]
  nil)

(defmethod next-state [:ev.g.u/configure]
  [state k ev]
  state)

(defmethod next-state [:ev.g.u/start]
  [state k ev]
  state)

(defmethod next-state [:ev.g.u/join]
  [state k ev]
  state)

(defmethod next-state [:ev.g.u/leave]
  [state k ev]
  state)

(defmethod next-state [:ev.g.p/move-cape]
  [state k ev]
  state)

(defmethod next-state [:ev.g.a/finish-game]
  [state k ev]
  state)

(s/fdef next-state
  :args (s/cat :state :g/game
               :k uuid?
               :ev :ev.g.m/event #_(s/alt :ev.p/move-cape :ev.a/finish-game)))

(comment

  (ns-unmap *ns* 'next-state)

  (stest/instrument [`next-state])
  (stest/unstrument [`next-state])

  (def state (gen/generate (s/gen :g/game)))
  (def ev (first (gen/sample (s/gen :ev.g.u/create) 1)))
  (s/explain :g/game (gen-default-game-state (java.util.UUID/randomUUID) ev))

  (def ev-p (gen/generate (s/gen :ev.p/move-cape)))
  (def ev-a (gen/generate (s/gen :ev.a/finish-game)))

  (def ev-p (first (gen/sample gen-ev-p-move-cape 1)))
  (def ev-a (first (gen/sample gen-ev-a-finish-game 1)))

  (next-state state ev-p)
  (next-state state ev-a)

  (next-state state (merge ev-p {:p/uuid "asd"}))



  ;;
  )



