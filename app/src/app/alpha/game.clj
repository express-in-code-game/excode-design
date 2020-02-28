(ns app.alpha.game
  (:require [clojure.repl :refer [doc]]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.spec.test.alpha :as stest]
            [app.alpha.spec :refer [gen-ev-p-move-cape
                                    gen-ev-a-finish-game]]))

(defmulti next-state (fn [st ev] [(:ev/type ev)]))

(defmethod next-state [:ev.p/move-cape]
  [st ev]
  :a-move-cape-event)

(defmethod next-state [:ev.a/finish-game]
  [st ev]
  :a-finish-game-event)

(s/fdef next-state
  :args (s/cat :st :g/state :ev :ev.g/event))

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



