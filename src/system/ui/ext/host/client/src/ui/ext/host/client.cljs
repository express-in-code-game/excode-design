(ns ui.ext.host.client
  (:require
   [clojure.core.async :as a :refer [<! >!  timeout chan alt! go
                                     alts!  take! put! mult tap untap
                                     pub sub sliding-buffer mix admix unmix]]
   [goog.string :as gstring]
   [goog.string.format]
   [ui.ext.host.chan :as host.chan]))


(defprotocol HostExtClient
  :extend-via-metadata true
  (register* [_ k ext])
  (unregister* [_ k ext]))

(defn create
  []
  (let [ops| (chan 10)
        ops|t (chan 10)
        ops|m (mult ops|t)]
    (do
      (let [channels (deref (resolve 'ui.ext.host/channels))]
        (admix (channels :ops|mx) ops|)
        (tap (channels :ops|m) ops|t)))
    (with-meta
      {:ops| ops|
       :ops|m ops|m}
      {'HostExtClient '_
       `register* (fn [_ k ext]
                    (put! ops| (host.chan/vl-register k ext)))
       `unregister* (fn [_ k ext]
                      (put! ops| (host.chan/vl-unregister k ext)))})))

(defn register
  [cl k ext]
  (register* cl k ext))

(defn unregister
  [cl k ext]
  (unregister* cl k ext))
