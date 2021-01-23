(ns deathstar.player-base.spec
  #?(:cljs (:require-macros [deathstar.player-base.spec]))
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::foo keyword?)
