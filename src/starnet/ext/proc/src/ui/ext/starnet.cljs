(ns ui.ext.starnet
  (:require
   [clojure.core.async :as a :refer [<! >!  timeout chan alt! go
                                     alts!  take! put! mult tap untap
                                     pub sub sliding-buffer mix admix unmix]]
   [goog.string :as gstring]
   [goog.string.format]
   [ui.ext.starnet.chan :as starnet.chan]
   [ui.ext.starnet.render :as render]
   [ui.ext.host.client :as host.client]
   [ui.ext.router.client :as router.client]
   [ui.ext.router.chan :as router.chan]))


(def host (host.client/create))

(def router (router.client/create))

(def channels (let [ops| (chan 10)]
                {:ops| ops|}))

(defn proc-ops
  []
  (let [{:keys [ops|]} channels]
    (go (loop []
          (when-let [v (<! ops|)]
            (condp = (starnet.chan/op v)
              (starnet.chan/op-render) (render/render))
            (recur))))
    (with-meta
      {}
      {})))

(defn -main
  []
  (println "ui.ext.starnet/-main")
  (let [ext (proc-ops)]
    (host.client/register host 'ui.ext.starnet ext))
  (put! (channels :ops|) (starnet.chan/vl-render)))