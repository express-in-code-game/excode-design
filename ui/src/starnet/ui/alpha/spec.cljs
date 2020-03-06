(ns starnet.ui.alpha.spec
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.spec.test.alpha :as stest]))

(derive cljs.core/Keyword :isa/keyword)