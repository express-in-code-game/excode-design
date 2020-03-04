(ns ui.alpha.spec
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.spec.test.alpha :as stest]))

(derive clojure.lang.Keyword :isa/keyword)