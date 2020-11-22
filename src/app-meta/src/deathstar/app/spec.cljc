(ns deathstar.app.spec
  #?(:cljs (:require-macros [deathstar.app.spec]))
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::game-id string?)

(s/def ::game (s/keys :req [::game-id]))
(s/def ::games (s/map-of string? ::game-state))
