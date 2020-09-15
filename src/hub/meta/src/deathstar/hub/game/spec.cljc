(ns deathstar.game.spec
  #?(:cljs (:require-macros [deathstar.game.spec]))
  (:require
   [clojure.spec.alpha :as s]))

(do (clojure.spec.alpha/check-asserts true))

(s/def ::uuid uuid?)

(s/def ::game (s/keys ::req [::uuid]))

(s/def ::games (s/map-of keyword? ::game))