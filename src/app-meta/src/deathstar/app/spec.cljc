(ns deathstar.app.spec
  #?(:cljs (:require-macros [deathstar.app.spec]))
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::frequency string?)
(s/def ::peer-id string?)
(s/def ::host-id ::peer-id)
(s/def ::peer-name string?)

(s/def ::peer-meta (s/keys :req [::peer-id
                                 ::peer-name]))
(s/def ::peer-metas (s/map-of ::peer-id ::peer-meta))
(s/def ::received-at some?)

(s/def ::tournament (s/keys :req [::frequency
                                  ::host-id
                                  ::peer-metas]))

(s/def ::tournaments (s/map-of string? ::tournament))



