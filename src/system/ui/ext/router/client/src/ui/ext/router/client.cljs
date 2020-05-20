(ns ui.ext.router.client
  (:require
   [clojure.core.async :as a :refer [<! >!  timeout chan alt! go
                                     alts!  take! put! mult tap untap
                                     pub sub sliding-buffer mix admix unmix]]
   [goog.string :as gstring]
   [goog.string.format]
   [ui.ext.router.chan :as ch]
   [ui.ext.host.client :as host.client]
   [ui.ext.host.chan :as host.chan]))


(defprotocol RouterExtClient
  :extend-via-metadata true
  (abc* [_]))

(def host (host.client/create))

(defn create
  []
  (let [routes| (chan (sliding-buffer 10))
        routes|m (mult routes|)
        {:keys [ops|m]} host
        resolve-channels #(resolve 'ui.ext.router/channels)
        on-fn (fn []
                (let [channels (deref (resolve-channels))]
                  (tap (channels :routes|m) routes|)))
        off-fn (fn []
                 (let [channels (deref (resolve-channels))]
                   (untap (channels :routes|m) routes|)))]
    (when (resolve-channels)
      (on-fn))
    (let [ext-register? (fn [v] (= (host.chan/op v) (host.chan/op-register)))
          ext-unregister? (fn [v] (= (host.chan/op v) (host.chan/op-unregister)))
          ext-self? (fn [v] (= (:k v) 'ui.ext.router))
          on| (tap ops|m (chan 10 (comp (filter (every-pred ext-register? ext-self?)))))
          off| (tap ops|m (chan 10 (comp (filter (every-pred ext-unregister?  ext-self?)))))]
      (go (loop []
            (alt!
              on| ([v] (on-fn))
              off| ([v] (off-fn)))
            (recur))))
    (go (loop []
          (when-let [v (<! (chan 1))]
            (do nil))))
    (with-meta
      {:routes| routes|
       :routes|m routes|m}
      {'RouterExtClient '_
       `abc* (fn [_])})))


