(ns deathstar.app.main
  (:gen-class)
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.string]

   [deathstar.app.spec :as app.spec]

   [deathstar.app.tray]
   [deathstar.app.reitit]
   [deathstar.app.docker-dgraph]
   [deathstar.app.dgraph]))

(def channels (merge
               (let [ops| (chan 10)]
                 {::app.spec/ops| ops|
                  ::app.spec/exit| (chan 1)})))

(def ctx {::app.spec/state* (atom {})})

(def dgraph-opts (deathstar.app.docker-dgraph/create-opts
                  {:deathstar.app.docker-dgraph/suffix "-main"}))

(defn create-proc-ops
  [channels ctx]
  (let [{:keys [::app.spec/ops| ::app.spec/exit|]} channels]
    (go
      (loop []
        (when-let [[value port] (alts! [ops| exit|])]
          (condp = port
            exit|
            (let []
              (println ::exit|)
              (<! (deathstar.app.docker-dgraph/down dgraph-opts))
              (println ::exiting)
              (System/exit 0))

            ops|
            (condp = (:op value)

              ::init
              (let [{:keys []} value]
                (println ::init)
                (<! (deathstar.app.tray/start {:deathstar.app.tray/exit| (::app.spec/exit| channels)}))
                (<! (deathstar.app.reitit/start channels))
                (<! (deathstar.app.reitit/start-static 3081))
                (<! (deathstar.app.reitit/start-static 3082))
                (<! (deathstar.app.docker-dgraph/count-images))
                (<! (deathstar.app.docker-dgraph/up dgraph-opts))
                (<! (deathstar.app.dgraph/load-schema))
                (println ::init-done)))))
        (recur)))))

;; (def _ (create-proc-ops channels {})) ;; cuases native image to fail

(defn -main [& args]
  (println ::-main)
  (create-proc-ops channels {})
  (put! (::app.spec/ops| channels) {:op ::init}))
