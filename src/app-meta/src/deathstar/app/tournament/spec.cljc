(ns deathstar.app.tournament.spec
  #?(:cljs (:require-macros [deathstar.app.tournament.spec]))
  (:require
   [clojure.spec.alpha :as s]
   [deathstar.app.spec :as app.spec]))

(s/def ::frequency string?)
(s/def ::host-id ::app.spec/peer-id)

(s/def ::tournament (s/keys :req [::frequency
                                  ::host-id
                                  ::peer-metas]))

(s/def ::tournaments (s/map-of string? ::tournament))