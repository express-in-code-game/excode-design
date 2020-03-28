(ns starnet.common.alpha.game.render
  (:require
   [clojure.repl :refer [doc]]
   [clojure.core.async :as a :refer [<! >!  timeout chan alt! go
                                     alts!  take! put! mult tap untap
                                     pub sub sliding-buffer mix admix unmix]]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [starnet.common.alpha.core :refer [make-inst with-gen-fmap]]
   [clojure.test :as test :refer [is are run-all-tests testing deftest run-tests]]

   [reagent.core :as r]))

(def ra-test (r/atom {:status :initial}))

(defn rc-game
  [channels ratoms]
  (let [{:keys [ch-inputs]} channels
        uuid* (r/cursor (ratoms :ra.g/state) [:g/uuid])
        status* (r/cursor (ratoms :ra.g/state) [:g/status])
        m-status* (r/cursor (ratoms :ra.g/map) [:m/status])
        ra-test-status* (r/cursor ra-test [:status])
        count-entities* (ratoms :ra.g/count-entities)
        timer* (r/atom 0)
        _ (go (loop []
                (<! (timeout 1000))
                (swap! timer* inc)
                (recur)))]
    (fn [_ _]
      (let [uuid @uuid*
            status @status*
            m-status  @m-status* #_(-> @(ratoms :ra.g/map) :m/status)
            count-entities @count-entities*
            ra-test-status @ra-test-status*
            timer @timer*]
        [:<>
         [:div  uuid]
         [:div  [:span "game status: "] [:span status]]
         [:div  [:span "map status: "] [:span (str m-status)]]
         [:div  [:span "total entities: "] [:span count-entities]]
         #_[:div  [:span "ra-test status: "] [:span (str ra-test-status)]]
         #_[:div  [:span "timer: "] [:span timer]]]))))

(comment

  (go
    (swap! ra-test assoc :status :starting)
    ;;  (<! (timeout 3000))
    ;; (println "hello" (-> @ra-test :status))
    (make-entities {})
    (swap! ra-test assoc :status :generating)
    (make-entities {})
    ;; (<! (timeout 3000))
    (swap! ra-test assoc :status :complete))

  (def -channels @(resolve 'starnet.common.alpha.game.store/-channels))

  (go
    (swap! ra-test assoc :status :starting)
    (let [c (chan 1)]
      (>! (-channels :ch-worker) {:worker/op :starnet.common.alpha.game.data/make-entities
                                  :ch/c-out c})
      (swap! ra-test assoc :status :generating)
      (let [o (<! c)]
        (swap! ra-test assoc :status [:complete (count o)]))))

  ;;
  )