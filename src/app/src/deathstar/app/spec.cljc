(ns deathstar.app.spec
  #?(:cljs (:require-macros [deathstar.app.spec]))
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::peer-id string?)
(s/def ::peer-name string?)

(s/def ::peer-meta (s/keys :req [::peer-id
                                 ::peer-name]))
(s/def ::peer-metas (s/map-of ::peer-id ::peer-meta))
(s/def ::received-at some?)