(ns starnet.ui.csp.main
  (:require
   [clojure.repl :refer [doc]]
   [reagent.core :as r]
   [clojure.core.async :as a :refer [<! >! timeout chan alt! go
                                     alts!  take! put!
                                     pub sub]]

   [clojure.spec.test.alpha :as stest]
   [clojure.spec.alpha :as s]

   [starnet.common.alpha.spec]

   [starnet.ui.alpha.spec]
   [starnet.ui.alpha.repl]
   [starnet.ui.alpha.tests]))



(defn ^:export main
  []
  (println "csp"))