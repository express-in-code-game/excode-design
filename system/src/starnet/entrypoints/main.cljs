(ns starnet.entrypoints.main
  (:require [starnet.alpha.main]))

(defn ^:export main
  []
  (apply starnet.alpha.main/-main args))