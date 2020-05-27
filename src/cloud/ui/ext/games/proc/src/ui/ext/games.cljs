(ns ui.ext.games
  (:require
   [clojure.core.async :as a :refer [<! >!  timeout chan alt! go
                                     alts!  take! put! mult tap untap
                                     pub sub sliding-buffer mix admix unmix]]
   [goog.string :as gstring]
   [goog.string.format]
   [ui.ext.games.chan :as games.chan]
   [ui.ext.games.render :as render]
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
        route-matches? (fn [v] (= (:handler v) :ext-games))
        routes| (tap (:routes|m router) (chan 10 (comp
                                                  (filter (every-pred route-matches?))
                                                  (map #(games.chan/vl-render)))))]
    (admix ops|mx routes|)
    (go (loop []
          (when-let [[v port] (alts! [ops| routes|])]
            (condp = port
              ops| (condp = (games.chan/op v)
                     (games.chan/op-render)  (render/render)))
            (recur))))
    (with-meta
      {}
      {})))

(defn -main
  []
  (println "ui.ext.games/-main")
  (let [ext (proc-ops)]
    (host.client/register host 'ui.ext.games ext))
  (put! (:ops| channels) (games.chan/vl-render)))