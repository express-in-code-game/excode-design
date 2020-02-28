(ns app.alpha.game
  (:require [clojure.repl :refer [doc]]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.spec.test.alpha :as stest]
            [app.alpha.spec :as spec]))

(defmulti next-state (fn [state event] [(:ev/type event)]))

(defmethod next-state [:ev.p/move-cape]
  [state event]
  :a-move-cape-event)

(defmethod next-state [:ev.a/finish-game]
  [state event]
  :a-finish-game-event)

(s/fdef next-state
  :args (s/cat :state :g/state :event :ev.g/event))

(comment
  
  (ns-unmap *ns* 'next-state)

  (def state (gen/generate (s/gen :g/state)))
  (def ev-p (gen/generate (s/gen :ev.p/move-cape)))
  (def ev-a (gen/generate (s/gen :ev.a/finish-game)))

  (next-state state ev-p)
  (next-state state ev-a)


  (stest/instrument [`next-state])
  (stest/unstrument [`next-state])

  ;;
  )



