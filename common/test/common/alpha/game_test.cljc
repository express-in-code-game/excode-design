(ns common.alpha.game-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test :refer [is run-all-tests testing deftest run-tests] :as t]
   [common.alpha.game :refer [gen-default-game-state]]))


(deftest gen-default-game-state-tests
  (testing "generates valid :g/game"
    (is (s/valid? :g/game
                  (gen-default-game-state
                   #uuid "46855899-838a-45fd-98b4-c76c08954645"
                   (sgen/generate (s/gen :ev.g.u/create)))))))

(comment
  
  (run-tests)
  
  ;;
  )