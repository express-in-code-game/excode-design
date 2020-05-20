(ns ui.ext.router
  (:require
   [clojure.core.async :as a :refer [<! >!  timeout chan alt! go
                                     alts!  take! put! mult tap untap
                                     pub sub sliding-buffer mix admix unmix]]
   [goog.string :as gstring]
   [goog.string.format]

   [bidi.bidi :as bidi]
   [pushy.core :as pushy]

   [ui.ext.host.client :as host.client]
   [ui.ext.router.chan :as router.chan]))


(def host (host.client/create))

(def channels (let [routes| (chan (sliding-buffer 10))
                    routes|m (mult routes|)]
                {:routes|  routes|
                 :routes|m routes|m}))

(defn proc-ops
  []
  
  (let [history (pushy/pushy
                 (fn [{:keys [url handler] :as pushed}]
                   #_(println "pushed" pushed)
                   #_(put! ch-history-states {:history/pushed pushed})
                   (put! (channels :routes|) (router.chan/vl-route url handler)))
                 (fn [url]
                   #_(println "url" url)
                   (merge
                    {:url url}
                    (bidi/match-route (deref (resolve 'app.main/routes)) url))))]
    (pushy/start! history)
    (go (loop []
          (when-let [v (<! (chan 1))]
            (do nil))))
    (with-meta
      {}
      {})))

(defn -main
  []
  (println "ui.ext.router/-main")
  (let [ext (proc-ops)]
    (host.client/register host 'ui.ext.router ext)))