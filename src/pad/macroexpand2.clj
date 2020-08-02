(ns pad.macroexpand2
  (:require
   [clojure.core.async :as a :refer [<! >! <!! timeout chan alt! go close!
                                     >!! <!! alt!! alts! alts!! take! put! mult tap untap
                                     thread pub sub sliding-buffer mix admix unmix]]
   [clojure.walk]
   [clojure.spec.alpha :as s]
   [clojure.test.check.generators :as gen]))

;; https://github.com/clojure/clojure/blob/clojure-1.10.1/src/clj/clojure/core.clj#L2776
(defmacro declare2
  "defs the supplied var names with no bindings, useful for making forward declarations."
  {:added "1.0"}
  [& names] `(do ~@(map #(list 'def (vary-meta % assoc :declared true)) names)))

(s/fdef declare2
  :args (s/cat :names (s/* #(= (count (str %)) 3)))
  :ret any?)

;; should throw, but does not
(defn f1
  []
  (go
    (declare2 foo abcd)))

;; throws, as expected
#_(defn f2
    []
    (declare2 foo abcd))

(comment

  (clojure.walk/macroexpand-all '(go (declare2 foo abcd)))

  (clojure.walk/macroexpand-all '(declare2 foo abcd))
  
  (clojure.walk/macroexpand-all '(let () :foo))
  
  (clojure.walk/macroexpand-all '(go (let () :foo)))
  
  
  (clojure.walk/macroexpand-all '(go
                                   (<! (chan 1))
                                   (let [] (declare2 foo abcd))))



  ;;
  )