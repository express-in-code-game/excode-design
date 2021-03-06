(ns deathstar.app.main
  (:gen-class)
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.string]

   [deathstar.app.spec :as app.spec]

   [deathstar.app.system-tray]
   [deathstar.app.reitit]
   [deathstar.app.docker-dgraph]
   [deathstar.app.dgraph]))

(defonce ^:private registry-ref (atom {}))

(defn create-opts
  [opts]
  {::id :main-deployment
   ::app.spec/state* (atom {})
   ::channels {::app.spec/system-exit| (chan 1)}
   ::system-tray? true
   ::dgraph-opts (deathstar.app.docker-dgraph/create-opts
                  {:deathstar.app.docker-dgraph/id :main
                   :deathstar.app.docker-dgraph/remove-volume? false})})

(defn unmount
  [{:keys [::id] :as opts}]
  (go
    (let [opts-in-registry (get @registry-ref id)
          {:keys [::system-tray?
                  ::dgraph-opts
                  ::channels]} opts-in-registry]
      (when system-tray?
        (<! (deathstar.app.system-tray/unmount {})))
      (<! (deathstar.app.reitit/stop channels {:deathstar.app.reitit/port 3080}))
      (<! (deathstar.app.reitit/stop-static {:deathstar.app.reitit/port 3081}))
      (<! (deathstar.app.reitit/stop-static {:deathstar.app.reitit/port 3082}))
      (<! (deathstar.app.docker-dgraph/down dgraph-opts))
      (<! (::procs-exit opts-in-registry))
      (swap! registry-ref dissoc id)
      (println ::unmount-done))))

(defn mount
  [{:keys [::id] :as opts}]
  (go
    (let [opts (merge (create-opts opts) opts)
          {:keys [::system-tray? ::dgraph-opts ::channels]
           {:keys [::app.spec/system-exit|]} ::channels} opts
          procs (atom [])
          procs-exit (fn []
                       (doseq [[exit| proc|] @procs]
                         (close! exit|))
                       (a/merge (mapv second @procs)))]
      (swap! registry-ref assoc id (merge
                                    opts
                                    {::procs-exit procs-exit}))
      (when system-tray?
        (<! (deathstar.app.system-tray/mount {:deathstar.app.system-tray/quit| (::app.spec/system-exit| channels)})))
      (<! (deathstar.app.reitit/start channels {:deathstar.app.reitit/port 3080}))
      (<! (deathstar.app.reitit/start-static {:deathstar.app.reitit/port 3081}))
      (<! (deathstar.app.reitit/start-static {:deathstar.app.reitit/port 3082}))
      (<! (deathstar.app.docker-dgraph/count-images))
      (<! (deathstar.app.docker-dgraph/up dgraph-opts))
      (<! (deathstar.app.dgraph/load-schema))

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
  (mount))
