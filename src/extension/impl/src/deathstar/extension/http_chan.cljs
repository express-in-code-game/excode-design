(ns deathstar.extension.http-chan
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

   [cljctools.proc.protocols :as proc.protocols]))


(defn create-channels
  []
  (let [request| (chan 10)
        request|m (mult request|)]
    {::request| request|
     ::request|m request|m}))

(defn create-proc-ops
  [channels ctx opts]
  (let [{:keys [::request|m]} channels
        {:keys [::url]} opts
        options (atom opts)
        request|t (tap request|m (chan 10))]
    (go
      (loop []
        (when-let [[v port] (alts! [request|t])]
          (condp = port
            request|t (let [{:keys [out|]} v]
                        (take! (http.client/post
                                url
                                {:transit-params (dissoc v :out|)})
                               (fn [resp]
                                 (println resp)
                                 (put! out| resp))))))
        (recur))
      (println (format "go block exiting %s" ::proc-ops)))))

