(ns app.impl.proc
  (:require
   [clojure.core.async :as a :refer [<! >! <!! timeout chan alt! go close!
                                     >!! <!! alt!! alts! alts!! take! put! mult tap untap
                                     thread pub sub sliding-buffer mix admix unmix]]
   [app.impl.protocols :as p]))

(defn create
  []
  (let []
    (reify
      p/Abc
      (foo [_] (chan 1))
      (bar [_] (chan 1)))))