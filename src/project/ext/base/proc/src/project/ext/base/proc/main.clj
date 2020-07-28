(ns project.ext.base.proc.main
  (:require
   [project.ext.base.meta.protocols :as base.p]
   [project.ext.base.proc.render :as base.render]
   [project.ext.scenarios.proc.main :as scenarios.main]))

(defn -main [& args]
  (base.render/-main))
