(ns starnet.app.alpha.core
  (:require
   [clojure.repl :refer [doc]]
   [clojure.core.async :as a :refer [<! >! <!! timeout chan alt! go
                                     >!! <!! alt!! alts! alts!! take! put!
                                     thread pub sub]]
   [clojure.java.io :as io]))

(defn authorized?
  [{:keys [ch-cruxdb] :as channels}])

(defn db-tx
  [{:keys [ch-cruxdb] :as channels} tx-data]
  (let [c-out (chan 1)]
    (put! ch-cruxdb {:cruxdb/op :tx
                     :cruxdb/tx-data tx-data
                     :ch/c-out c-out})
    c-out))

(defn create-user
  [channels tx-data]
  (db-tx channels tx-data))

