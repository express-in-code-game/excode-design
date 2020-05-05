(ns system.ui.main
  (:require
   [clojure.core.async :as async :refer [<! >!  chan go alt! take! put!  alts! pub sub]]
   [system.common.core]))

(defn ^:export main
  []
  (println "; main"))

(comment

  (system.common.core/foo)

  (+ 1 2)

  ;;
  )

