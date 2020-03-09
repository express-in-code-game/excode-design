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

(s/def :g.e/uuid uuid?)
(s/def :g.e/pos (s/tuple int? int?))
(s/def :g.e/numeric-value number?)
(s/def :g.e/type keyword?)

(s/def :g.e.type/teleport (s/keys :req [:g.e/type :g.e/uuid :g.e/pos]))
(s/def :g.e.type/cape (s/keys :req [:g.e/type :g.e/uuid  :g.e/pos]))
(s/def :g.e.type/value-tile (s/keys :req [:g.e/type :g.e/uuid :g.e/pos :g.e/numeric-value]))

(s/def :g.p/cape :g.e.type/cape)
(s/def :g.p/entities (s/keys :req [:g.p/cape]))
(s/def :g.p/sum number?)

(s/def :g.p/player (s/keys :req [:g.p/entities :g.p/sum]))

(s/def :g.r/host (s/nilable boolean?))
(s/def :g.r/player (s/nilable int?))
(s/def :g.r/observer (s/nilable boolean?))
(s/def :g.r/role (s/keys :req [:g.r/host :g.r/player :g.r/observer]))

(def setof-game-status #{:created :opened :started :finished})

(s/def :g/uuid uuid?)
(s/def :g/status setof-game-status)
(s/def :g/start-inst inst?)
(s/def :g/duration-ms number?)
(s/def :g/map-size (s/tuple int? int?))
(s/def :g/roles (s/map-of uuid? :g.r/role))
(s/def :g/player-states (s/map-of int? :g.p/player))
(s/def :g/exit-teleports (s/coll-of :g.e.type/teleport))
(s/def :g/value-tiles (s/coll-of :g.e.type/value-tile))

(s/def :g/game (s/keys :req [:g/uuid :g/status
                             :g/duration-ms :g/start-inst
                             :g/roles :g/player-states
                             :g/value-tiles :g/exit-teleports
                             :g/map-size]))

(s/def :ev.g.u/create (with-gen-fmap
                        (s/keys :req [:ev/type :u/uuid]
                                :opt [])
                        #(assoc %  :ev/type :ev.g.u/create)))

(s/def :ev.g.u/delete (with-gen-fmap
                        (s/keys :req [:ev/type]
                                :opt [])
                        #(assoc %  :ev/type :ev.g.u/delete)))

(s/def :ev.g.u/configure (with-gen-fmap
                           (s/keys :req [:ev/type :u/uuid :g/uuid]
                                   :opt [])
                           #(assoc %  :ev/type :ev.g.u/configure)))

(s/def :ev.g.u/start (with-gen-fmap
                       (s/keys :req [:ev/type :u/uuid :g/uuid]
                               :opt [])
                       #(assoc %  :ev/type :ev.g.u/start)))

(s/def :ev.g.u/join (with-gen-fmap
                      (s/keys :req [:ev/type :u/uuid :g/uuid]
                              :opt [])
                      #(assoc %  :ev/type :ev.g.u/join)))

(s/def :ev.g.u/leave (with-gen-fmap
                       (s/keys :req [:ev/type :u/uuid :g/uuid]
                               :opt [])
                       #(assoc %  :ev/type :ev.g.u/leave)))

(s/def :ev.g.u/update-role (with-gen-fmap
                             (s/keys :req [:ev/type :u/uuid :g/uuid :g.r/role]
                                     :opt [])
                             #(assoc %  :ev/type :ev.g.u/update-role)))

(s/def :ev.g.p/move-cape (with-gen-fmap
                           (s/keys :req [:ev/type :u/uuid :g/uuid
                                         :g.p/cape])
                           #(assoc %  :ev/type :ev.g.p/move-cape)))

(s/def :ev.g.p/collect-tile-value (with-gen-fmap
                                    (s/and (s/keys :req [:ev/type :u/uuid]))
                                    #(assoc %  :ev/type :ev.g.p/collect-tile-value)))

(s/def :ev.g.a/finish-game (with-gen-fmap
                             (s/and (s/keys :req [:ev/type :u/uuid]))
                             #(assoc %  :ev/type :ev.g.a/finish-game)))

(def setof-ev-g-p-event
  #{:ev.g.p/move-cape :ev.g.p/collect-tile-value})

(defmulti ev-game-player (fn [x] (:ev/type x)))
(defmethods-for-a-set ev-game-player setof-ev-g-p-event)
(s/def :ev.g.p/event (s/multi-spec ev-game-player :ev/type))

(defmulti ev-game-arbiter (fn [x] (:ev/type x)))
(defmethod ev-game-arbiter :ev.g.a/finish-game [x] :ev.g.a/finish-game)
(s/def :ev.g.a/event (s/multi-spec ev-game-arbiter :ev/type))

(def setof-ev-g-m-event
  #{:ev.g.p/move-cape :ev.g.p/collect-tile-value :ev.g.a/finish-game})

(defmulti ev-game-member (fn [x] (:ev/type x)))
(defmethods-for-a-set ev-game-member setof-ev-g-m-event)
(s/def :ev.g.m/event (s/multi-spec ev-game-member :ev/type))

(def setof-ev-g-u-event
  #{:ev.g.u/create :ev.g.u/delete :ev.c/delete-record
    :ev.g.u/configure :ev.g.u/start :ev.g.u/join
    :ev.g.u/leave})

(defmulti ev-game-user (fn [x] (:ev/type x)))
(defmethods-for-a-set ev-game-user setof-ev-g-u-event)
(s/def :ev.g.u/event (s/multi-spec ev-game-user :ev/type))

(def setof-ev-g-event
  #{:ev.g.u/create :ev.g.u/delete :ev.c/delete-record
    :ev.g.u/configure :ev.g.u/start :ev.g.u/join :ev.g.u/update-role
    :ev.g.u/leave :ev.g.p/move-cape
    :ev.g.p/collect-tile-value :ev.g.a/finish-game})

(defmulti ev-game (fn [x] (:ev/type x)))
(defmethods-for-a-set ev-game setof-ev-g-event)
(s/def :ev.g/event (s/multi-spec ev-game :ev/type))

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

(s/fdef make-game-state
  :args (s/cat)
  :ret :g/game)

(defmulti next-game-state
  "Returns the next state of the game."
  {:arglists '([state key event])}
  (fn [state k ev] [(:ev/type ev)]))

(defmethod next-game-state [:ev.c/delete-record]
  [state k ev]
  nil)

(defmethod next-game-state [:ev.g.u/create]
  [state k ev]
  (-> state
      (update-in [:g/roles]
                 assoc (:u/uuid ev) {:g.r/observer true
                                     :g.r/host true
                                     :g.r/player nil})))

(defmethod next-game-state [:ev.g.u/delete]
  [state k ev]
  nil)

(defmethod next-game-state [:ev.g.u/configure]
  [state k ev]
  (when state
    (merge state ev)))

(defmethod next-game-state [:ev.g.u/start]
  [state k ev]
  state)

(defmethod next-game-state [:ev.g.u/join]
  [state k ev]
  (update-in state [:g/roles]
             assoc (ev :u/uuid) {:g.r/observer true
                                 :g.r/host false
                                 :g.r/player nil}))

(defmethod next-game-state [:ev.g.u/update-role]
  [state k ev]
  (update-in state [:g/roles]
             update (ev :u/uuid) merge (:g.r/role ev)))

(defmethod next-game-state [:ev.g.u/leave]
  [state k ev]
  (update-in state [:g/roles]
             dissoc (ev :u/uuid)))

(defmethod next-game-state [:ev.g.p/move-cape]
  [state k ev]
  state)

(defmethod next-game-state [:ev.g.a/finish-game]
  [state k ev]
  state)

(defmethod next-game-state [:ev.g.p/collect-tile-value]
  [state k ev]
  state)

(s/fdef next-game-state
  :args (s/cat :state (s/nilable :g/game)
               :k uuid?
               :ev :ev.g/event)
  :ret (s/nilable :g/game))


(comment

  (ns-unmap *ns* 'next-game-state)
  (stest/instrument [`next-game-state])
  (stest/unstrument [`next-game-state])

  (gen/sample (s/gen :ev.g.u/update-role) 10)
  (gen/sample (s/gen :ev.g.u/create) 10)
  (gen/sample (s/gen :g.r/role) 10)


  (stest/check `next-game-state)

  ;;
  )

(def users {:A {:u/name "mighty A"
                :u/alias :A
                :u/uuid  (gen/generate gen/uuid)}
            :B {:u/name "curious B"
                :u/alias :B
                :u/uuid  (gen/generate gen/uuid)}
            :C {:u/name "new C"
                :u/alias :C
                :u/uuid  (gen/generate gen/uuid)}
            :D {:u/name "D the seeker"
                :u/alias :D
                :u/uuid  (gen/generate gen/uuid)}
            :E {:u/name "E'vo"
                :u/alias :E
                :u/uuid  (gen/generate gen/uuid)}})

(def state (atom (make-game-state)))

(defn next-state
  [ev]
  (reset! state (next-game-state
                 @state
                 (:g/uuid @state)
                 (assoc ev :g/uuid (@state :g/uuid))))
  nil)

(defn user-uuid
  [u-alias]
  (-> users u-alias :u/uuid))

(defn user-alias
  [u-uuid]
  (some (fn [[k {:u/keys [uuid alias]}]]
          (when (= uuid u-uuid) alias))
        users))

(defn to-aliaskey
  [m]
  (reduce
   (fn [ag [k v]]
     (assoc ag (user-alias k) v)) {} m))

(defn make-role-update
  [x]
  (cond
    (= x :observer) {:g.r/observer true}
    (= x :host) {:g.r/host true}
    (:g.r/player x) (merge x {:g.r/observer false})
    :else x))

(comment
  (s/explain (s/map-of keyword? (s/keys :req [:u/name :u/uuid])) users)

  @state

  (next-state {:ev/type :ev.g.u/create
               :u/uuid  (user-uuid :A)})

  (doseq [alias [:B :C :D :E]]
    (next-state {:ev/type :ev.g.u/join
                 :u/uuid (user-uuid alias)}))

  ; ? roles 
  (->> @state :g/roles to-aliaskey)

  (next-state {:ev/type :ev.g.u/leave
               :u/uuid  (user-uuid :D)})

  (next-state {:ev/type :ev.g.u/join
               :u/uuid  (user-uuid :D)})

  (next-state {:ev/type :ev.g.u/update-role
               :u/uuid  (user-uuid :A)
               :g.r/role (make-role-update {:g.r/player 0})})

  (next-state {:ev/type :ev.g.u/update-role
               :u/uuid  (user-uuid :B)
               :g.r/role (make-role-update {:g.r/player 1})})

  ;;
  )

(comment

  (gen/sample (gen/map gen/keyword gen/boolean) 5)
  (gen/sample (gen/tuple gen/nat gen/boolean gen/ratio))
  (gen/sample (gen/large-integer* {:min 0}))
  (gen/sample (gen/large-integer* {:min 50 :max 100}))
  (gen/sample (gen/large-integer* {:min 50 :max 100}))
  (gen/sample
   (gen/frequency [[5 gen/small-integer] [3 (gen/vector gen/small-integer)] [2 gen/boolean]]))

  (def five-through-nine (gen/choose 5 9))
  (gen/sample five-through-nine)

  (def languages (gen/elements ["clojure" "haskell" "erlang" "scala" "python"]))
  (gen/sample languages)
  (def int-or-nil (gen/one-of [gen/small-integer (gen/return nil)]))
  (gen/sample int-or-nil)
  (def mostly-ints (gen/frequency [[9 gen/small-integer] [1 (gen/return nil)]]))
  (->> (gen/sample mostly-ints 10000) (filter nil?) (count))

  (def even-and-positive (gen/fmap #(* 2 %) gen/nat))
  (gen/sample even-and-positive 20)

  (def powers-of-two (gen/fmap #(int (Math/pow 2 %)) gen/nat))
  (gen/sample powers-of-two)
  (def sorted-vec (gen/fmap sort (gen/vector gen/small-integer)))
  (gen/sample sorted-vec)

  (def anything-but-five (gen/such-that #(not= % 5) gen/small-integer))
  (gen/sample anything-but-five)

  (def vector-and-elem (gen/bind (gen/not-empty (gen/vector gen/small-integer))
                                 #(gen/tuple (gen/return %) (gen/elements %))))
  (gen/sample vector-and-elem)

  (gen/sample (gen/elements [:foo :bar :baz]))
  (gen/sample (gen/elements #{:foo :bar :baz}) 3)
  
  ;;
  )


(def tags #{:entity :cape :knowledge :bio :building :combinable :element})


(s/def :g.e.prop/resolve (s/with-gen int?
                           #(gen/choose 100 1000)))
(s/def :g.e.prop/vision (s/with-gen int?
                          #(gen/choose 4 16)))
(s/def :g.e.prop/energy (s/with-gen int?
                          #(gen/choose 0 100)))
(s/def :g.e/tags (s/with-gen (s/coll-of keyword?)
                   #(gen/list-distinct (gen/elements tags) {:num-elements 3})))

(s/def :g.e/cape (s/keys :req [:g.e.prop/resolve
                               :g.e.prop/vision
                               :g.e.prop/energy
                               :g.e/tags]))

(comment

  (gen/generate (gen/list-distinct (gen/elements tags) {:num-elements 3}))
  (gen/generate (s/gen :g.e/tags))
  (gen/generate (s/gen :g.e/cape))

  (= (fd/-intersection (fd/interval 0 20) (fd/interval 10 30)) (fd/interval 10 20))

  (= (into #{}
           (run* [q]
                 (fresh [x y z]
                        (fd/in x y z (fd/interval 1 10))
                        (fd/+ x y z)
                        (fd/< x y)
                        (== z 10)
                        (== q [x y z]))))
     (into #{} '([1 9 10] [2 8 10] [3 7 10] [4 6 10])))

  (= (into #{}
           (run* [q]
                 (fresh [x y z]
                        (fd/in x y z (fd/interval 1 3))
                        (fd/distinct [x y z])
                        (== q [x y z]))))
     (into #{} '([1 2 3] [1 3 2] [2 1 3] [2 3 1] [3 1 2] [3 2 1])))

  (run* [q]
        (fresh [x y z]
               (fd/in x y z (fd/interval 1 20))
               (fd/+ x y z)
               (fd/distinct [x y z])
               (== z 15)
               (== q [x y z])))

  (->>
   (run* [q]
         (fresh [x y]
                (fd/in x y (fd/interval 1 64))
                (== q [x y])))
   (count))

  (= (fd/-drop-before (fd/multi-interval 2 4) (fd/-lb 3))
     4)

  (= (fd/-keep-before (fd/multi-interval 2 4) (fd/-lb 3))
     2)

  (fd/-keep-before (fd/interval 0 100) (fd/-lb 50))
  (fd/-drop-before (fd/interval 0 100) (fd/-lb 50))
  (fd/-difference 1 2)

  (fd/multi-interval  (fd/interval 0 100) (fd/interval 10 30))
  (= (fd/-difference (fd/interval 0 100) (fd/interval 10 30))
     (fd/multi-interval  (fd/interval 0 9) (fd/interval 31 100)))
  (= (fd/-intersection (fd/interval 0 100) (fd/interval 10 30))
     (fd/interval 10 30))

  (def i (fd/interval 1 5))
  (.-lb i)
  (.-ub i)
  (fd/-lb i)
  (fd/-ub i)

  (def entities
    {:database {:energy {:op '- :min 30 :max 50}
                :knowledge {:op '+ :min 20 :max 30}
                :available [even?]}
     :fruit-tree {:energy {:op '+ :min 60 :max 80}
                  :vision {:op '+ :min 30 :max 50}
                  :available [#(not= 2)]}
     :update-center {:resolve {:op '+ :min 20 :max 50}
                     :vision {:op '+ :min 10 :max 50}
                     :interface {:op '+ :min 20 :max 60}
                     :energy {:op '- :min 20 :max 60}
                     :available [odd?]}
     :nanite-lab {:range {:op '+ :min 20 :max 80}
                  :fields {:op '+ :min 10 :max 30}
                  :energy {:op '- :min 20 :max 40}
                  :available [#{4 5 6 7} 'after-update-center]}
     :missiondroid {:range {:min 0 :max 100}
                    :fields {:min 0 :max 100}
                    :interface {:min 0 :max 100}}
     :cape {:energy {:min 0 :max 100 :min-start 50 :max-start 70}
            :resolve {:min 0 :max 100 :min-start 80 :max-start 90}
            :vision {:min 0 :max 100  :min-start 80 :max-start 100}
            :knowledge {:min 0 :max 100 :min-start 20 :max-start 70}}})

  (defn gen-cape
    []
    {:energy (gen/generate (gen/choose (get-in entities [:cape :energy :min-start])
                                       (get-in entities [:cape :energy :max-start])))
     :resolve (gen/generate (gen/choose (get-in entities [:cape :resolve :min-start])
                                        (get-in entities [:cape :resolve :max-start])))
     :vision (gen/generate (gen/choose (get-in entities [:cape :vision :min-start])
                                       (get-in entities [:cape :vision :max-start])))
     :knowledge (gen/generate (gen/choose (get-in entities [:cape :knowledge :min-start])
                                          (get-in entities [:cape :knowledge :max-start])))})

  (def c1 (gen-cape))

  (run 5 [q]
       (fresh [a b c d]
              (?== a 5)
              (fd/in a b c d (fd/interval 1 100))
              (fd/in b (fd/interval 20 80))
              (fd/in c (fd/interval 50 60))
              (fd/in d (fd/interval 10 30))
              #_(fd/eq
                 (= (- b c) 20)
                 (= (+ (* c 2) (* d 4)) 24))
              (?== q {:a a
                      :b b
                      :c c
                      :d d})))

  (run 10 [q]
       (fresh [a]
              (fd/in a (fd/interval 1 5))
              (?== q {:a a})))
  ;;
  )


(comment

  



  ;;
  )
