(ns starnet.entrypoints.main
  (:require
   [starnet.alpha.main]))

(defn ^:export main
  [& args]
  (apply starnet.alpha.main/main args))