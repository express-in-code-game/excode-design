(ns app.api
  (:require
   [clojure.core.async :as a :refer [<! >! <!! timeout chan alt! go close!
                                     >!! <!! alt!! alts! alts!! take! put! mult tap untap
                                     thread pub sub sliding-buffer mix admix unmix]]

   [app.impl.protocols :as p]
   [app.impl.proc :as impl.proc]))

(defn create-proc []
  (impl.proc/create))

(defn foo [proc]
  (p/foo proc))

(defn bar [proc]
  (p/bar proc))

