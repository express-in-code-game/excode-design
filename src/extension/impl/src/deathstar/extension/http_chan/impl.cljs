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

   [cljs.nodejs :as node]
   
   [cljctools.csp.op.spec :as op.spec]

   [deathstar.extension.http-chan.chan :as http-chan.chan]
   [deathstar.extension.spec :as extension.spec]))

(def http (node/require "http"))

(defn create-proc-ops
  [channels opts]
  (let [{:keys [::http-chan.chan/request|m
                ::http-chan.chan/response|]} channels
        {:keys [::connect-opts-fn]}  opts
        http-opts-fn (fn []
                       (let [{:keys [::port
                                     ::host
                                     ::path]}  (connect-opts-fn)]
                         #js {:host host
                              :port port
                              :path path
                              :method "POST"
                              :headers #js {"Content-Type" "application/edn"}}))
        request|t (tap request|m (chan 10))]
    (go
      (loop []
        (when-let [[v port] (alts! [request|t])]
          (condp = port
            request|t (let [{:keys [::op.spec/out|]} v]
                        (println (dissoc v ::op.spec/out|))
                        (doto
                         (http.request (http-opts-fn))
                          (.write (pr-str (dissoc v ::op.spec/out|)))
                          (.on "response" (fn [res]
                                            (.on res "data"
                                                 (fn [chunk]
                                                   (println (type chunk))
                                                   (println (.toString chunk))
                                                   (let [value (-> chunk
                                                                   (.toString)
                                                                   (read-string))]
                                                     (put! response| value)
                                                     (put! out| value))))))
                          (.end))
                        #_(take! (http.client/post
                                  (url-fn)
                                  {#_:transit-params :edn-params (dissoc v ::op.spec/out|)})
                                 (fn [resp]
                                   (println resp)
                                   (put! response| resp)
                                   (put! out| resp))))))
        (recur))
      (println (format "go block exiting %s" ::proc-ops)))))


(comment
  
  
  
  ;;
  )
