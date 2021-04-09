(ns tiefighter.peer.main
  (:gen-class)
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >! <!! >!!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.string]

   [tiefighter.peer.spec :as app.spec]

   [tiefighter.peer.reitit]
   [tiefighter.peer.dgraph]
   [tiefighter.peer.libp2p]))

(defonce ^:private registry-ref (atom {}))

(defn create-opts
  [{:keys [::id] :as opts}]
  {::id id
   ::app.spec/state* (atom {})
   ::channels {::app.spec/system-exit| (chan 1)}
   ::port 3080})

(def peer1-preset (create-opts
                   {::id :peer1
                    ::port 3081}))

(def peer2-preset (create-opts
                   {::id :peer2
                    ::port 3082}))

(defn unmount
  [{:keys [::id] :as opts}]
  (go
    (let [{:keys [::dgraph-opts
                  ::channels
                  ::port]} opts]
      (<! (tiefighter.peer.reitit/stop channels {:tiefighter.peer.reitit/port port}))
      (let [opts-in-registry (get @registry-ref id)]
        (when (::procs-exit opts-in-registry)
          (<! ((::procs-exit opts-in-registry)))))
      (swap! registry-ref dissoc id)
      (println ::unmount-done))))

(defn mount
  [{:keys [::id] :as opts}]
  (go
    (let [opts (merge (create-opts opts) opts)
          {:keys [::dgraph-opts
                  ::channels
                  ::port]
           {:keys [::app.spec/system-exit|]} ::channels} opts
          procs (atom [])
          procs-exit (fn []
                       (doseq [[exit| proc|] @procs]
                         (close! exit|))
                       (a/merge (mapv second @procs)))]
      (swap! registry-ref assoc id (merge
                                    opts
                                    {::procs-exit procs-exit}))
      (<! (tiefighter.peer.reitit/start channels {:tiefighter.peer.reitit/port port}))
      #_(<! (tiefighter.peer.dgraph/ready?))
      (<! (tiefighter.peer.dgraph/upload-schema))

      (let [exit| (chan 1)
            proc|
            (go
              (loop []
                (when-let [[value port] (alts! [system-exit| exit|])]
                  (condp = port

                    exit|
                    (do nil)

                    system-exit|
                    (do
                      (let []
                        (println ::exit|)
                        (<! (unmount opts))
                        (println ::exiting)
                        (System/exit 0))))))
              (println ::go-block-exits))]
        (swap! procs conj [exit| proc|]))

      (println ::mount-done))))

(defn -main [& args]
  (println ::-main)
  (a/<!! (mount peer1-preset)))
