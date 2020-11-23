(ns deathstar.scenario.spec
  #?(:cljs (:require-macros [deathstar.scenario.spec]))
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::foo keyword?)
