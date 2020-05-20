(ns ui.ext.events
  (:require
   [clojure.core.async :as a :refer [<! >!  timeout chan alt! go
                                     alts!  take! put! mult tap untap
                                     pub sub sliding-buffer mix admix unmix]]
   [goog.string :as gstring]
   [goog.string.format]
   [ui.ext.events.chan :as events.chan]
   [ui.ext.events.render :as render]
   [ui.ext.host.client :as host.client]
   [ui.ext.router.client :as router.client]
   [ui.ext.router.chan :as router.chan]))


(def host (host.client/create))

(def router (router.client/create))

(def channels (let [ops| (chan 10)
                    ops|mx (mix ops|)]
                {:ops| ops|
                 :ops|mx ops|mx}))

(defn proc-ops
  []
  (let [{:keys [ops| ops|mx]} channels
        route-matches? (fn [v] (= (:handler v) :ext-events))
        routes| (tap (:routes|m router) (chan 10 (comp
                                                  (filter  (every-pred route-matches?))
                                                  (map #(events.chan/vl-render)))))]
    (admix ops|mx routes|)
    (go (loop []
          (when-let [[v port] (alts! [ops| routes|])]
            (condp = port
              ops| (condp = (events.chan/op v)
                     (events.chan/op-render)  (render/render)))
            (recur))))
    (with-meta
      {}
      {})))

(defn -main
  []
  (println "ui.ext.events/-main")
  (let [ext (proc-ops)]
    (host.client/register host 'ui.ext.events ext))
  (put! (:ops| channels) (events.chan/vl-render)))