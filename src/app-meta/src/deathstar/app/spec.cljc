(ns deathstar.app.spec
  #?(:cljs (:require-macros [deathstar.app.spec]))
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::game-id string?)

(s/def ::game (s/keys :req [::game-id]))
(s/def ::games (s/map-of string? ::game-state))

(s/def ::peer-id string?)

(s/def ::peer-meta (s/keys :req [::peer-id]))

(s/def ::peer-metas (s/map-of ::peer-id ::peer-meta))

(s/def ::received-at some?)
