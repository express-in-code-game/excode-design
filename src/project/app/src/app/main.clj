(ns app.main
  (:require
   [project.ext.base.main]
   [project.ext.scenarios.main]
   [project.ext.connect.main]
   [project.ext.server.main]
   [project.ext.games.main]))

(defn -main [& args]
  (project.ext.base.main/mount)
  (project.ext.scenarios.main/mount)
  (project.ext.connect.main/mount)
  (project.ext.server.main/mount)
  (project.ext.games.main/mount))