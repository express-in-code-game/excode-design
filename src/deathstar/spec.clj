(ns deathstar.spec
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::peer-id string?)
(s/def ::peer-name string?)

(s/def ::peer-meta (s/keys :req [::peer-id
                                 ::peer-name]))
(s/def ::peer-metas (s/map-of ::peer-id ::peer-meta))
(s/def ::received-at some?)


(s/def ::username string?)
(s/def ::password string?)

(s/def ::user-info (s/keys :req [::username]))