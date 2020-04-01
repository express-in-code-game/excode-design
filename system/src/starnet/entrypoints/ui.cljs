(ns starnet.entrypoints.ui
  (:require [starnet.alpha.main]))

(defn ^:export main
  []
  (apply starnet.alpha.main/-main args))