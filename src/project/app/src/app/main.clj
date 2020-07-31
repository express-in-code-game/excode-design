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
  (<!! (proc-main))

  (project.ext.scenarios.main/mount)
  (project.ext.connect.main/mount)
  (project.ext.server.main/mount)
  (project.ext.games.main/mount))

(defn proc-main-f
  [channels ctx]
  (let [ops| (chan 10)]
    (go (loop []
          (when-let [{:keys [op opts out|]} (<! ops|)]
            (condp = op
              :mount (let []
                       (prn :mount)
                       (<! (core.p/mount* proc-render {}))
                       (<! (core.p/mount* proc-store {}))
                       (put! out| 123)
                       (close! out|))
              :unmount (future (let []
                                 (prn :unmount))))))
        (println ";; proc-main exiting"))
    (reify
      core.p/Mountable
      (core.p/mount* [_ opts] (put! ops| {:op :mount}))
      (core.p/unmount* [_ opts] (put! ops| {:op :unmount})))))