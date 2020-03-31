(ns starnet.common.alpha.game.core
  (:require
   [clojure.repl :refer [doc]]
   [clojure.core.async :as a :refer [<! >!  timeout chan alt! go
                                     alts!  take! put! mult tap untap
                                     pub sub sliding-buffer mix admix unmix]]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [starnet.common.alpha.core :refer [make-inst with-gen-fmap]]

   [starnet.common.alpha.spec]

   [clojure.test :as test :refer [is are run-all-tests testing deftest run-tests]]
   #?(:cljs [starnet.common.alpha.macros :refer-macros [defmethod-set derive-set]]
      :clj  [starnet.common.alpha.macros :refer [defmethod-set derive-set]])))

