(ns deathstar.app.spec
  #?(:cljs (:require-macros [deathstar.app.spec]))
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::frequency string?)

(s/def ::tournament (s/keys :req [::frequency]))
(s/def ::tournaments (s/map-of string? ::tournament))

(s/def ::peer-id uuid?)

(s/def ::host-id uuid?)

(s/def ::peer-meta (s/keys :req [::peer-id]))

(s/def ::peer-metas (s/map-of ::peer-id ::peer-meta))

(s/def ::received-at some?)
