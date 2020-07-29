(ns app.main
  (:require
   [project.ext.base.api]))

(defn -main [& args]
  (project.ext.base.api/mount))