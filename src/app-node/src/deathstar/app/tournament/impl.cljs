(ns deathstar.app.tournament.impl
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.core.async.impl.protocols :refer [closed?]]
   [clojure.string :as string]
   #?(:cljs [cljs.core.async.interop :refer-macros [<p!]])
   #?(:cljs [goog.string.format])
   #?(:cljs [goog.string :refer [format]])
   #?(:cljs [goog.object])
   #?(:cljs [cljs.reader :refer [read-string]])

   [cljctools.csp.op.spec :as op.spec]
   [cljctools.cljc.core :as cljc.core]


   [deathstar.app.spec :as app.spec]
   [deathstar.app.chan :as app.chan]

   [deathstar.app.tournament.spec :as app.tournament.spec]
   [deathstar.app.tournament.chan :as app.tournament.chan]))

(defonce fs (js/require "fs"))
(defonce path (js/require "path"))

(defn create-state*
  []
  (atom {}))

(defn create-proc-ops
  [channels ctx opts]
  (let [{:keys [::app.tournament.chan/ops|]} channels
        {:keys [::app.spec/state*
                ::app.spec/ipfs*
                ::app.spec/orbitdb*
                ::app.spec/tournament-channels*
                ::app.spec/tournament-eventlogs*
                ::app.spec/tournaments-kvstore*]} ctx]
    (go
      (loop []
        (when-let [[value port] (alts! [ops|])]
          (condp = port
            ops|
            (condp = (select-keys value [::op.spec/op-key ::op.spec/op-type ::op.spec/op-orient])
              
              
              
              
              ))
          
          (recur)
          )))))