(ns deathstar.ui.scenario.impl
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

   [deathstar.ui.scenario.spec :as ui.scenario.spec]
   [deathstar.ui.scenario.chan :as ui.scenario.chan]

   [deathstar.app.spec :as app.spec]
   [deathstar.app.chan :as app.chan]))


(defn create-proc-ops
  [channels ctx opts]
  (let [{:keys [::ui.scenario.chan/ops|
                ::ui.scenario.chan/release|]} channels
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
            (condp = (select-keys value [::op.spec/op-key ::op.spec/op-type ::op.spec/op-orient])

              {::op.spec/op-key ::ui.scenario.chan/init
               ::op.spec/op-type ::op.spec/fire-and-forget}
              (let [{:keys []} value]
                (println ::init))))

          (recur))))))