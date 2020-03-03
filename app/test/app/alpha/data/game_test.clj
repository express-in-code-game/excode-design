(ns app.alpha.data.game-test
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as sgen]
            [clojure.spec.test.alpha :as stest]
            [clojure.test :refer [is run-all-tests testing deftest run-tests] :as t]
   
            [app.alpha.data.game :refer [gen-default-game-state]]))


(deftest gen-default-game-state-tests
  (testing "generates valid :g/game"
    (is (s/valid? :g/game
                  (gen-default-game-state
                   (java.util.UUID/randomUUID)
                   (sgen/generate (s/gen :ev.g.u/create)))))))

(comment
  
  (run-tests)
  
  ;;
  )