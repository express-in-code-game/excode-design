(ns app.alpha.data.game
  (:require [clojure.repl :refer [doc]]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.spec.test.alpha :as stest]
            [app.alpha.spec :refer [gen-ev-p-move-cape
                                    gen-ev-a-finish-game]]))

(defmulti next-state (fn [st ev] [(:ev/type ev)]))

(defmethod next-state [:ev.g.p/move-cape]
  [st ev]
  :a-move-cape-event)

(defmethod next-state [:ev.g.a/finish-game]
  [st ev]
  :a-finish-game-event)

(defmethod next-state [:ev.g.u/create]
  [st ev]
  :ev.g.u/create)

(defmethod next-state [:ev.g.u/delete]
  [st ev]
  :ev.g.u/delete)

(defmethod next-state [:ev.g.u/configure]
  [st ev]
  :ev.g.u/configure)

(defmethod next-state [:ev.g.u/start]
  [st ev]
  :ev.g.u/start)

(defmethod next-state [:ev.g.u/join]
  [st ev]
  :ev.g.u/join)

(defmethod next-state [:ev.g.u/leave]
  [st ev]
  :ev.g.u/leave)

(s/fdef next-state
  :args (s/cat :st :g/state
               :ev :ev.g.m/event #_(s/alt :ev.p/move-cape :ev.a/finish-game)))

(comment

  (ns-unmap *ns* 'next-state)

  (stest/instrument [`next-state])
  (stest/unstrument [`next-state])

  (def state (gen/generate (s/gen :g/state)))

  (def ev-p (gen/generate (s/gen :ev.p/move-cape)))
  (def ev-a (gen/generate (s/gen :ev.a/finish-game)))

  (def ev-p (first (gen/sample gen-ev-p-move-cape 1)))
  (def ev-a (first (gen/sample gen-ev-a-finish-game 1)))

  (next-state state ev-p)
  (next-state state ev-a)

  (next-state state (merge ev-p {:p/uuid "asd"}))



  ;;
  )



