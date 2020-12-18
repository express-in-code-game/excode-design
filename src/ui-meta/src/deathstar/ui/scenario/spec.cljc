(ns deathstar.ui.scenario.spec
  #?(:cljs (:require-macros [deathstar.ui.scenario.spec]))
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::page-events keyword?)
(s/def ::page-game keyword?)
(s/def ::scenario-origin string?)
