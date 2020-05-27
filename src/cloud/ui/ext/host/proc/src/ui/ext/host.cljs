(ns ui.ext.host
  (:require
   [clojure.core.async :as a :refer [<! >!  timeout chan alt! go
                                     alts!  take! put! mult tap untap
                                     pub sub sliding-buffer mix admix unmix]]
   [goog.string :as gstring]
   [goog.string.format]
   [ui.ext.host.protocol :as p]
   [ui.ext.host.chan :as host.chan]))



(def channels (let [ops| (chan 10)
                    ops|m (mult ops|)
                    ops|mx (mix ops|)]
                {:ops| ops|
                 :ops|m ops|m
                 :ops|mx ops|mx}))

(def state (atom {:extensions {}}))


(defn proc-ops
  []
  (let [{:keys [ops|m]} channels
        ops| (tap ops|m (chan 10))]
    (go (loop []
          (when-let [v (<! ops|)]
            (condp (host.chan/op v)
                   (host.chan/op-register) (let [{:keys [k ext]} v]
                                             #_(prn "host.chan/op-register" v)
                                             (swap! state update-in [:extensions] assoc k ext))
                   (host.chan/op-unregister) (let [{:keys [k ext]} v]
                                               (swap! state update-in [:extensions] dissoc k)))
            (recur))))
    (reify p/HostExt
      (abc* [_]))))

(defn -main
  []
  (println "ui.ext.host/-main")
  (let [ext (proc-ops)]
    (put! (channels :ops|) (host.chan/vl-register 'ui.ext.host ext))))
