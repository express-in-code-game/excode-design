(ns deathstar.ui.tournament.impl
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [cljs.core.async.impl.protocols :refer [closed?]]
   [cljs.core.async.interop :refer-macros [<p!]]
   [goog.string.format :as format]
   [goog.string :refer [format]]
   [goog.object]
   [clojure.string :as str]
   [cljs.reader :refer [read-string]]

   [cljctools.csp.op.spec :as op.spec]
   [cljctools.cljc.core :as cljc.core]

   [deathstar.ui.spec :as ui.spec]
   [deathstar.ui.chan :as ui.chan]

   [deathstar.ui.tournament.spec :as ui.tournament.spec]
   [deathstar.ui.tournament.chan :as ui.tournament.chan]

   [deathstar.app.spec :as app.spec]
   [deathstar.app.chan :as app.chan]

   [deathstar.app.tournament.spec :as app.tournament.spec]
   [deathstar.app.tournament.chan :as app.tournament.chan]))


(defn create-proc-ops
  [channels ctx opts]
  (let [{:keys [::ui.tournament.chan/ops|
                ::ui.tournament.chan/release|]} channels
        release
        (fn []
          (go
            (do nil)))]
    (go
      (loop []
        (when-let [[value port] (alts! [ops| release|])]
          (condp = port

            release|
            (let [{:keys [::op.spec/out|]} value]
              (<! (release))
              (close! out|))

            ops|
            (do
              (condp = (select-keys value [::op.spec/op-key ::op.spec/op-type ::op.spec/op-orient])

                {::op.spec/op-key ::ui.tournament.chan/foo
                 ::op.spec/op-type ::op.spec/fire-and-forget}
                (let [{:keys []} value]
                  (println ::foo)))
              (recur)))))
      (println ::go-block-exits))))