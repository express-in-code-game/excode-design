(ns deathstar.extension.http-chan.impl
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [goog.string :refer [format]]
   [clojure.string :as string]
   [cljs.reader :refer [read-string]]
   [clojure.pprint :refer [pprint]]
   [cljs-http.client :as http.client]

   [deathstar.extension.http-chan.chan :as http-chan.chan]
   [deathstar.extension.spec :as extension.spec]))

(defn create-proc-ops
  [channels state]
  (let [{:keys [::http-chan.chan/request|m]} channels
        url-fn (fn []
                 (let [{:keys [::extension.spec/server-port
                               ::extension.spec/server-host
                               ::extension.spec/http-path]}  @state]
                   (str "http://" server-host ":" server-port http-path)))
        request|t (tap request|m (chan 10))]
    (go
      (loop []
        (when-let [[v port] (alts! [request|t])]
          (condp = port
            request|t (let [{:keys [out|]} v]
                        (take! (http.client/post
                                (url-fn)
                                {:transit-params (dissoc v :out|)})
                               (fn [resp]
                                 (println resp)
                                 (put! out| resp))))))
        (recur))
      (println (format "go block exiting %s" ::proc-ops)))))

