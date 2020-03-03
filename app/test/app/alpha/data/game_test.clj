(ns app.alpha.data.game-test
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.spec.test.alpha :as stest]

            [app.alpha.data.game :refer [gen-default-game-state]]))

(s/fdef gen-default-game-state
  :args (s/cat)
  :ret :g/game)

