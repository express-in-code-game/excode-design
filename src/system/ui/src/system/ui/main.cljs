(ns system.ui.main
  (:require
   [clojure.core.async :as async :refer [<! >!  chan go alt! take! put!  alts! pub sub]]
   [system.common.spec]))

(defn ^:export main
  []
  (println "; main"))

(comment

  (system.common.spec/foo)

  (+ 1 2)

  ;;
  )

