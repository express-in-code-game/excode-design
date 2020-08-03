(ns pad.clj1
  (:require
   [clojure.core.async :as a :refer [<! >! <!! timeout chan alt! go close!
                                     >!! <!! alt!! alts! alts!! take! put! mult tap untap
                                     thread pub sub sliding-buffer mix admix unmix]]
   [clojure.walk]
   [clojure.spec.alpha :as s]
   [clojure.test.check.generators :as gen]))


(defn f1
  [{:keys [a.b/c]}]
  (prn c))

(comment

  (f1 {:a.b/c 3})

  ;;
  )

(defmacro assert-keys
  [x]
  `~x)

(defmacro lets
  [bindings & body]
  (assert-keys 'a.b/c)
  `(let []))

(defn f2
  [v]
  (lets [{:keys []} v]
        (prn c)))

(comment

  (assert-keys 'a.b/c)
  (macroexpand '(assert-keys 3))
  
  ;;
  )

