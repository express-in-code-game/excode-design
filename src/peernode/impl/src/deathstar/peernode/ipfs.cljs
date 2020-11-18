(ns deathstar.peernode.ipfs
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
   [cljctools.cljc.core :as cljc.core]))

(def fs (node/require "fs"))
(def os (node/require "os")) 
(def path (node/require "path"))
(def IPFS (node/require "ipfs-core"))
(def ipfs-cli (node/require "ipfs-cli"))
(def cli (node/require "/ctx/js-ipfs/packages/ipfs-cli/test/utils/cli"))

(defn create-proc-ops
  []
  (go
    (try
      (cli "daemon")
      #_(let [ipfs (<p! (IPFS.create
                         (clj->js {:silent false
                                   :repoAutoMigrate true
                                   :repo (getRepoPath)
                                   :init {:allowNew true}
                                   :start true
                                   :pass nil})))]
          {:ipfs ipfs
           :cleanup (fn []
                      (.stop ipfs))})

      (catch js/Error err (do
                            (println "caught")
                            (println err))))))