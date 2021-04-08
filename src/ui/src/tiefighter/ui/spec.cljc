(ns tiefighter.ui.spec
  #?(:cljs (:require-macros [tiefighter.ui.spec]))
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::foo keyword?)