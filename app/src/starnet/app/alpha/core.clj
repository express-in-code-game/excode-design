(ns starnet.app.alpha.core
  (:require
   [clojure.repl :refer [doc]]
   [clojure.core.async :as a :refer [<! >! <!! timeout chan alt! go
                                     >!! <!! alt!! alts! alts!! take! put!
                                     thread pub sub]]
   [clojure.java.io :as io]))

(defn authorized?
  [a b c]
  (go
    
    )
  )