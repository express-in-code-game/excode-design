(ns deathstar.peernode.main
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [cljs.core.async.interop :refer-macros [<p!]]
   [goog.string.format :as format]
   [goog.string :refer [format]]
   [goog.object]
   [clojure.string :as string]
   [cljs.reader :refer [read-string]]
   [cljs.nodejs :as node]

   [cljctools.csp.op.spec :as op.spec]
   [cljctools.cljc.core :as cljc.core]

   [cljctools.rsocket.impl]
   [cljctools.rsocket.examples-nodejs]))

(def fs (node/require "fs"))
(def path (node/require "path"))

(def ^:dynamic daemon nil)

(comment

  (type daemon)
  (js/Object.keys daemon)
  (go
    (let [id (<p! (daemon._ipfs.id))]
      (println (js-keys id))
      (println (.-id id))
      (println (format "id is %s" id))))

  ;;
  )

(defn main [d]
  (println ::main)
  (println (js-keys d._ipfs))
  (set! daemon d))

(def exports #js {:main main})

(when (exists? js/module)
  (set! js/module.exports exports))