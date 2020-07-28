(ns project.ext.base.proc.main
  (:require
   [project.ext.base.proc.render :as proc.render]))

(defn -main [& args]
  (proc.render/-main))
