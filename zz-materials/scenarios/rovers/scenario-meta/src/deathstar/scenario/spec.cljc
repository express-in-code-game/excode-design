(ns deathstar.scenario.spec
  #?(:cljs (:require-macros [deathstar.scenario.spec]))
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]))

; https://github.com/DeathStarGame/starnet/tree/9002a81708a2317cbff88817093bba6182d0f110/system/test/starnet/pad
; https://github.com/DeathStarGame/starnet/blob/9002a81708a2317cbff88817093bba6182d0f110/system/test/starnet/pad/game2.cljc
; https://github.com/DeathStarGame/starnet/blob/9002a81708a2317cbff88817093bba6182d0f110/system/test/starnet/pad/game3/data.cljc
; https://github.com/DeathStarGame/starnet/blob/9002a81708a2317cbff88817093bba6182d0f110/system/test/starnet/pad/game1.cljc
; https://github.com/DeathStarGame/starnet/blob/af86204ff94776ceab140208f5a6e0d654d30eba/common/test/starnet/common/pad/reagent1.cljs


(def ^:const x-size 90)
(def ^:const y-size 60)
(def ^:const box-size-px 10)

(s/def ::id uuid?)
(s/def ::entity-type keyword?)
(s/def ::x (s/with-gen
             int?
             #(gen/choose 0 x-size)))
(s/def ::y (s/with-gen
             int?
             #(gen/choose 0 y-size)))


(s/def ::x-offset int?)
(s/def ::y-offset int?)

(s/def ::energy-level number?)
(s/def ::energy number?)
(s/def ::rover-vision-range int?)
(s/def ::rover-scan-range int?)

(s/def ::scan-upgrade number?)
(s/def ::move-upgrade number?)
(s/def ::signal-tower-upgrade number?)
(s/def ::recharge-upgrade number?)


(s/def ::choose-location #{::closest ::highest-energy ::lowest-energy})
(s/def ::location-type #{::recharge ::signal-tower})
(s/def ::choose-upgrade #{::move ::signal-tower ::recharge ::scan})

(s/def ::rover (s/merge
                (s/keys :req [::id])
                (s/with-gen
                  (s/keys :req [::x
                                ::y
                                ::entity-type
                                ::energy-level

                                ::scan-upgrade
                                ::move-upgrade
                                ::signal-tower-upgrade
                                ::recharge-upgrade

                                ::rover-vision-range
                                ::rover-scan-range])
                  #(gen/hash-map
                    ::entity-type (gen/return ::rover)
                    ::x (gen/choose 26 34)
                    ::y (gen/choose 12 16)
                    ::energy-level (gen/return 100)
                    ::rover-vision-range (gen/return 4)
                    ::rover-scan-range (gen/return 8)

                    ::scan-upgrade (gen/return 1)
                    ::move-upgrade (gen/return 1)
                    ::signal-tower-upgrade (gen/return 1)
                    ::recharge-upgrade (gen/return 1)))))


(s/def ::signal-tower (s/merge
                       (s/keys :req [::id])
                       (s/with-gen
                         (s/keys :req [::x
                                       ::y
                                       ::entity-type])
                         #(gen/hash-map
                           ::entity-type (gen/return ::signal-tower)
                           ::x (gen/choose 0 x-size)
                           ::y (gen/choose 0 y-size)
                           ::energy-max (gen/return -5)
                           ::energy-min (gen/return -20)
                           ::energy (gen/choose -20 -5)))))
(derive ::signal-tower ::entity)


(s/def ::recharge (s/merge
                   (s/keys :req [::id])
                   (s/with-gen
                     (s/keys :req [::energy ::entity-type])
                     #(gen/hash-map
                       ::entity-type (gen/return ::recharge)
                       ::x (gen/choose 0 x-size)
                       ::y (gen/choose 0 y-size)
                       ::energy-max (gen/return 30)
                       ::energy-min (gen/return 10)
                       ::energy (gen/choose 10 30)))))
(derive ::recharge ::entity)


(s/def ::sands (s/merge
                (s/keys :req [::id])
                (s/with-gen
                  (s/keys :req [::energy ::entity-type])
                  #(gen/hash-map
                    ::entity-type (gen/return ::sands)
                    ::x (gen/choose 0 x-size)
                    ::y (gen/choose 0 y-size)
                    ::energy (gen/choose -20 -5)))))
(derive ::sands ::entity)

(defmulti entity-mm (fn [ent] (::entity-type ent)))
(defmethod entity-mm ::signal-tower [ent] ::signal-tower)
(defmethod entity-mm ::recharge [ent] ::recharge)
(defmethod entity-mm ::rover [ent] ::rover)
(defmethod entity-mm ::sands [ent] ::sands)
(s/def ::entity (s/with-gen
                  (s/multi-spec entity-mm ::entity-type)
                  #(gen/frequency
                    [[200 (s/gen ::sands)]
                     [30 (s/gen ::recharge)]
                     [20 (s/gen ::signal-tower)]
                     [1 (s/gen ::rover)]])))
(def entity-gen (s/gen ::entity))
(def rover-gen (s/gen ::rover))

(def rovers-gen (gen/vector rover-gen 4 7))

(s/def ::entities (s/with-gen
                    (s/map-of uuid? ::entity)
                    #(gen/hash-map
                      (gen/generate gen/uuid) entity-gen)))

(s/def ::state (s/keys :req [::entities
                             ::hovered-entity
                             ::visited-locations
                             ::entities-in-rovers-range
                             ::entities-in-rovers-range-per-rover
                             ::locations
                             ::rovers
                             ::visited-locations]))



(comment

  (isa? ::rover ::entity)
  (gen/generate (s/gen ::id))
  (gen/generate (s/gen ::sands))
  (gen/generate (s/gen ::rover))
  (gen/generate (s/gen ::recharge))
  (gen/generate (s/gen ::entity))

  (gen/generate (s/gen ::entity) 30 42)

  (= (gen/generate rovers-gen 30 42)
     (gen/generate rovers-gen 30 42))

  (gen/generate (gen/vector entity-gen 3) 30 42)

  (gen/sample (s/gen ::entities) 5)

  (gen/sample (s/gen ::entity) 5)


  (gen-entities 3 3)


  ;;
  )