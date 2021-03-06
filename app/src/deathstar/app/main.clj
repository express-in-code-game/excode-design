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

(defn create-channels
  []
  (merge
   (let [ops| (chan 10)]
     {::app.spec/ops| ops|
      ::app.spec/system-exit| (chan 1)})))

(defn create-ctx
  [opts]
  {::id :main-deployment
   ::app.spec/state* (atom {})
   ::system-tray? true
   ::dgraph-opts (deathstar.app.docker-dgraph/create-opts
                  {:deathstar.app.docker-dgraph/suffix "-main"
                   :deathstar.app.docker-dgraph/remove-volume? false})})

(defn mount
  [{:keys [::id ::ctx ::channels] :as opts}]
  (go
    (let [ctx (or ctx (create-ctx opts))
          channels (or channels (create-channels))
          {:keys [::app.spec/system-exit|]} channels
          {:keys [::system-tray? ::dgraph-opts]} opts
          procs (atom [])
          procs-exit (fn []
                       (doseq [[exit| proc|] @procs]
                         (close! exit|))
                       (a/merge (mapv second @procs)))]
      (swap! registry-ref assoc id {::ctx ctx
                                    ::channels channels
                                    ::procs-exit procs-exit})
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
                (when-let [[value port] (alts! [ops| exit|])]
                  (condp = port

                    exit|
                    (do nil)

                    system-exit|
                    (do
                      (let []
                        (println ::exit|)
                        (<! (unmount channels ctx))
                        (println ::exiting)
                        (System/exit 0))))))
              (println ::go-block-exits))]
        (swap! procs conj [exit| proc|]))

      (println ::mount-done))))

(defn unmount
  [{:keys [::id] :as opts}]
  (go
    (let [{:keys [::app.spec/ops|
                  ::app.spec/exit|]} channels
          {:keys [::system-tray?
                  ::dgraph-opts]} ctx
          deployment (get @registry-ref id)]
      (when system-tray?
        (<! (deathstar.app.system-tray/unmount {:deathstar.app.system-tray/exit| (::app.spec/exit| channels)})))
      (<! (deathstar.app.reitit/stop channels {:deathstar.app.reitit/port 3080}))
      (<! (deathstar.app.reitit/stop-static {:deathstar.app.reitit/port 3081}))
      (<! (deathstar.app.reitit/stop-static {:deathstar.app.reitit/port 3082}))
      (<! (deathstar.app.docker-dgraph/down dgraph-opts))
      (<! (::procs-exit deployment))
      (swap! registry-ref dissoc id)
      (println ::unmount-done))))

(defn -main [& args]
  (println ::-main)
  (mount))
