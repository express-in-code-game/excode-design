(ns deathstar.ui.spec
  #?(:cljs (:require-macros [deathstar.ui.spec]))
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::page-events keyword?)
(s/def ::page-game keyword?)
