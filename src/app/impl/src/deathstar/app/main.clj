(ns deathstar.app.main
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.string :as str]

   [cljctools.csp.op.spec :as op.spec]
   [cljctools.cljc.core :as cljc.core]

   [cljctools.rsocket.impl]
   [cljctools.rsocket.impl.examples]

   [deathstar.app.spec :as app.spec]
   [deathstar.app.chan :as app.chan]

   [deathstar.peernode.spec :as peernode.spec]
   [deathstar.peernode.chan :as peernode.chan]))

(defn -main [& args]
  (println ::-main))

(comment
  
  (cljc.core/rand-uuid)
  
  
  ;;
  )