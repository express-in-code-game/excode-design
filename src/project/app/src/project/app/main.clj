(ns project.app.main
  (:require
   [clojure.core.async :as a :refer [<! >! <!! timeout chan alt! go close!
                                     >!! <!! alt!! alts! alts!! take! put! mult tap untap
                                     thread pub sub sliding-buffer mix admix unmix]]
   [clojure.spec.alpha :as s]
   [clojure.tools.cli :refer [parse-opts]]

   [project.core.protocols :as p]
   [project.core]
   [project.spec :as sp]
   [project.ext.base.main]
   [project.ext.scenarios.main]
   [project.ext.connect.main]
   [project.ext.server.main]
   [project.ext.games.main]))


(def channels (let []
                {}))

(def ctx (atom {:exts {}}))


;; does not work
(defn proc-main-f
  [channels ctx]
  (let [ops| (chan 10)
        loop| (go (loop []
                    (when-let [{:keys [op opts out|]} (<! ops|)]
                      (condp = op
                        (sp/op :main/ops| ::mount1) (let [exts {:project.ext/scenarios (project.ext.scenarios.main/mount channels ctx)
                                                                :project.ext/connect (project.ext.connect.main/mount channels ctx)
                                                                :project.ext/server (project.ext.server.main/mount channels ctx)
                                                                :project.ext/games (project.ext.games.main/mount channels ctx)}]
                                                      (prn ::mount)
                                                      (swap! ctx update :exts merge exts)
                                                      (put! out| true)
                                                      (close! out|))
                        (sp/op :main/ops| ::unmount) (future (let []
                                                               (prn ::unmount)))))
                    (recur))
                  (println ";; proc-main exiting"))]
    (with-meta
      {:loop| loop|}
      {'p/Mountable '_
       `p/mount* (fn [_ opts] (put! ops| (sp/vl :main/ops| {:op ::mount})))
       `p/unmount* (fn [_ opts] (put! ops| {:op ::unmount}))})
    #_(reify
        p/Mountable
        (p/mount* [_ opts] (put! ops| {:op :mount}))
        (p/unmount* [_ opts] (put! ops| {:op :unmount})))))


(def proc-main (proc-main-f channels ctx))

(defn -main [& args]
  #_(p/mount* proc-main {})
  (prn "main")
  (prn args)
  #_(<!! (:loop| proc-main)))
