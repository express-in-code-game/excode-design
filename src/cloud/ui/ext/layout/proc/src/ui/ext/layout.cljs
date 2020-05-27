(ns ui.ext.layout
  (:require
   [clojure.core.async :as a :refer [<! >!  timeout chan alt! go
                                     alts!  take! put! mult tap untap
                                     pub sub sliding-buffer mix admix unmix]]
   [goog.string :as gstring]
   [goog.string.format]

   [reagent.core :as r]

   [ui.ext.layout.chan :as layout.chan]
   [ui.ext.layout.render :as render]
   [ui.ext.host.client :as host.client]
   [ui.ext.router.client :as router.client]
   [ui.ext.router.chan :as router.chan]))

(def host (host.client/create))

(def router (router.client/create))

(def channels (let [ops| (chan 10)]
                {:ops| ops|}))

(def ratom (r/atom {:handler nil
                    :url nil}))

(defn proc-ops
  []
  (let [{:keys [ops|]} channels
        {:keys [routes|m]} router
        routes| (tap routes|m (chan 10))]
    (go (loop []
          (when-let [[v port] (alts! [ops| routes|])]
            (condp = port
              routes| (r/rswap! ratom merge (select-keys v [:handler :url]))
              ops| (condp = (layout.chan/op v)
                     (layout.chan/op-render) (render/render channels ratom)))
            (recur))))
    (with-meta
      {}
      {})))

(defn -main
  []
  (println "ui.ext.layout/-main")
  (let [ext (proc-ops)]
    (host.client/register host 'ui.ext.layout ext))
  (put! (channels :ops|) (layout.chan/vl-render)))