(ns app.main
  (:require
   [project.ext.base.main]
   [project.ext.scenarios.main]))

(defn -main [& args]
  (project.ext.base.main/mount)
  (project.ext.scenarios.main/mount))