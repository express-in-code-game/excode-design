(ns starnet.common.pad.logic2
  (:require
   [clojure.set :refer [subset?]]
   [clojure.walk :as walk]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check :as tc]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [clojure.test.check.clojure-test :refer [defspec]]

   [clojure.core.logic.nominal :as nom]
   [clojure.core.logic :as l]
   [clojure.core.logic.protocols :as lprot]
   [clojure.core.logic.pldb :as pldb]
   [clojure.core.logic.fd  :as fd]
   [clojure.core.logic.unifier :as u]
   [clojure.core.logic.arithmetic :as la]
   [clojure.test :as test :refer [is are run-all-tests testing deftest run-tests]]))

(comment

  
  ;;
  )