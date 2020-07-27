(ns app.main
  (:require
   [app.render]
   [scenario.rovers.main]))

(defn -main [& args]
  (app.render/render))

