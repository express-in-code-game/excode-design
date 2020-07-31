(ns app.main
  (:require
   [project.core.protocols :as p]
   [project.core]
   [project.ext.base.main]
   [project.ext.scenarios.main]
   [project.ext.connect.main]
   [project.ext.server.main]
   [project.ext.games.main]))

(declare proc-main-f)

(def channels (let []
                {}))

(def ctx (atom {:exts {}}))

(def proc-main (proc-main-f channels ctx))

(defn -main [& args]
  (p/mount* proc-main {})
  (<!! (:loop| proc-main)))

(defn proc-main-f
  [channels ctx]
  (let [ops| (chan 10)
        loop| (go (loop []
                    (when-let [{:keys [op opts out|]} (<! ops|)]
                      (condp = op
                        :mount (let [exts {:project.ext/scenarios (project.ext.scenarios.main/mount channels ctx)
                                           :project.ext/connect (project.ext.connect.main/mount channels ctx)
                                           :project.ext/server (project.ext.server.main/mount channels ctx)
                                           :project.ext/games (project.ext.games.main/mount channels ctx)}]
                                 (prn :mount)
                                 (swap! ctx update :exts merge exts)
                                 (put! out| true)
                                 (close! out|))
                        :unmount (future (let []
                                           (prn :unmount)))))
                    (recur))
                  (println ";; proc-main exiting"))]
    (with-meta
      {:loop| loop|}
      {'p/Mountable '_
       `p/mount* (fn [_ opts] (put! ops| {:op :mount}))
       `p/unmount* (fn [_ opts] (put! ops| {:op :unmount}))})
    #_(reify
        p/Mountable
        (p/mount* [_ opts] (put! ops| {:op :mount}))
        (p/unmount* [_ opts] (put! ops| {:op :unmount})))))