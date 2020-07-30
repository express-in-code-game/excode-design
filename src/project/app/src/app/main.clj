(ns app.main
  (:require
   [project.ext.base.main]))

(defn -main [& args]
  (project.ext.base.main/mount))