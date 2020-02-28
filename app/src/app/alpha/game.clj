(ns app.alpha.game
  (:require [clojure.repl :refer [doc]]
            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [app.alpha.spec :as spec]))



(defmulti next-state (fn [state event] [(:event/type event)]))

(defmethod next-state [:event/player-event]
  [state event]
  :a-player-event)

(defmethod next-state [:event/arbiter-event]
  [state event]
  :an-arbiter-event)

(s/fdef next-state
  :args (s/cat :state some? :event some?))

(comment

  (next-state {} {:event/type :event/player-event})
  (next-state {} {:event/type :event/arbiter-event})


  (stest/instrument [`next-state])
  (stest/unstrument [`next-state])

  ;;
  )



