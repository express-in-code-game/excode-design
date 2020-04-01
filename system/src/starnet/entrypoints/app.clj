(ns starnet.entrypoints.app
  (:require [starnet.alpha.main]))

(defn -main  [& args]
  (apply starnet.alpha.main/-main args))