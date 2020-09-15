(ns deathstar.hub.tap.remote.spec
  #?(:cljs (:require-macros [deathstar.hub.tap.remote.spec]))
  (:require
   [clojure.spec.alpha :as s]

   [deathstar.user.spec :as user.spec]
   [deathstar.game.spec :as game.spec]))

(do (clojure.spec.alpha/check-asserts true))

(s/def ::loading? boolean?)

#_(s/def ::users-games ::game.spec/games #_(s/map-of ::game.spec/game))
