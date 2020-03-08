(ns starnet.common.alpha.game
  (:require
   [clojure.repl :refer [doc]]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [starnet.common.alpha.core :refer [make-inst with-gen-fmap]]
   #?(:cljs [starnet.common.alpha.macros :refer-macros [defmethods-for-a-set]]
      :clj  [starnet.common.alpha.macros :refer [defmethods-for-a-set]])

   [clojure.core.logic.nominal :exclude [fresh hash] :as nom]
   [clojure.core.logic :exclude [is] :as l :rename {== ?==} :refer :all]
   [clojure.core.logic.pldb :as pldb :refer [db with-db db-rel db-fact]]
   [clojure.core.logic.fd  :as fd]
   [clojure.core.logic.unifier :as u]
   [clojure.test :as test :refer [is are run-all-tests testing deftest run-tests]]))

(defn make-game-state
  []
  {:g/uuid (gen/generate gen/uuid)
   :g/status :created
   :g/start-inst (make-inst)
   :g/duration-ms 60000
   :g/roles {}
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
                      (vec))
   :g/map-size [16 16]})

(defmulti next-state-game
  "Returns the next state of the game."
  {:arglists '([state key event])}
  (fn [state k ev] [(:ev/type ev)]))

(defmethod next-state-game [:ev.c/delete-record]
  [state k ev]
  nil)

(defmethod next-state-game [:ev.g.u/create]
  [state k ev]
  (-> state
      (update-in [:g/roles]
                 assoc (:u/uuid ev) {:g.r/observer true
                                     :g.r/host true
                                     :g.r/player nil})))

(defmethod next-state-game [:ev.g.u/delete]
  [state k ev]
  nil)

(defmethod next-state-game [:ev.g.u/configure]
  [state k ev]
  (when state
    (merge state ev)))

(defmethod next-state-game [:ev.g.u/start]
  [state k ev]
  state)

(defmethod next-state-game [:ev.g.u/join]
  [state k ev]
  (update-in state [:g/roles]
             assoc (ev :u/uuid) {:g.r/observer true
                                 :g.r/host false
                                 :g.r/player nil}))

(defmethod next-state-game [:ev.g.u/update-role]
  [state k ev]
  (update-in state [:g/roles]
             update (ev :u/uuid) merge (:g.r/role ev)))

(defmethod next-state-game [:ev.g.u/leave]
  [state k ev]
  (update-in state [:g/roles]
             dissoc (ev :u/uuid)))

(defmethod next-state-game [:ev.g.p/move-cape]
  [state k ev]
  state)

(defmethod next-state-game [:ev.g.a/finish-game]
  [state k ev]
  state)

(defmethod next-state-game [:ev.g.p/collect-tile-value]
  [state k ev]
  state)


(comment

  (ns-unmap *ns* 'next-state-game)
  (stest/instrument [`next-state-game])
  (stest/unstrument [`next-state-game])

  (gen/sample (s/gen :ev.g.u/update-role) 10)
  (gen/sample (s/gen :ev.g.u/create) 10)
  (gen/sample (s/gen :g.r/role) 10)


  (stest/check `next-state-game)

  ;;
  )
