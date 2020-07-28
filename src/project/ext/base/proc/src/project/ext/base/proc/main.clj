(ns project.ext.base.proc.main
  (:require
   [project.ext.base.meta.protocols :as base.p]
   [project.ext.render.proc.main :as render.main]
   [project.ext.scenarios.proc.main :as scenarios.main]
   [project.ext.console.proc.main :as console.main]))

(defn -main [& args]
  (render.main/-main))
