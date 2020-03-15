(ns starnet.app.alpha.crux
  (:require
   [clojure.repl :refer [doc]]
   [clojure.core.async :as a :refer [<! >! <!! timeout chan alt! go
                                     >!! <!! alt!! alts! alts!! take! put!
                                     thread pub sub]]
   [crux.api :as crux]
   [clojure.java.io :as io]))

(def conf {:crux.node/topology '[crux.kafka/topology
                                 crux.kv.rocksdb/kv-store]
           :crux.kafka/bootstrap-servers "broker1:9092"
           :crux.kafka/tx-topic "crux-transaction-log"
           :crux.kafka/doc-topic "crux-docs"
           :crux.kafka/create-topics true
           :crux.kafka/doc-partitions 1
           :crux.kafka/replication-factor (short 1)
           :crux.kv/db-dir "/ctx/data/crux"
           :crux.kv/sync? false
           :crux.kv/check-and-store-index-version true})

(defn easy-ingest
  "Uses Crux put transaction to add a vector of documents to a specified
  node"
  [node docs]
  (crux/submit-tx node
                  (vec (for [doc docs]
                         [:crux.tx/put doc]))))

; not used in the system, for repl purposes only
(def ^:private node nil)

(defn proc-dbcall
  [f args cout]
  (go
    (let [x (f args)]
      (>! cout x))))

(defn proc-cruxdb
  [psys cdb]
  (let [c (chan 1)]
    (sub psys :cruxdb c)
    (go (loop [node nil]
          (if-let [[vl port] (alts! (if node [c cdb] [c]))] ; add check if node is valid
            (condp = port
              c (condp = (second vl)
                  :start (let [n (crux/start-node conf)]
                           (alter-var-root #'node (constantly n))
                           (println "; crux node started")
                           (recur n))
                  :close (do
                           (.close node)
                           (alter-var-root #'node (constantly nil))
                           (println "; crux node closed")
                           (recur nil)))
              cdb (do
                    (apply proc-dbcall vl)
                    (recur node)))))
        (println "closing proc-cruxdb"))))

(comment

  (easy-ingest
   node
   [{:crux.db/id :commodity/Pu
     :common-name "Plutonium"
     :type :element/metal
     :density 19.816
     :radioactive true}

    {:crux.db/id :commodity/N
     :common-name "Nitrogen"
     :type :element/gas
     :density 1.2506
     :radioactive false}

    {:crux.db/id :commodity/CH4
     :common-name "Methane"
     :type :molecule/gas
     :density 0.717
     :radioactive false}

    {:crux.db/id :commodity/Au
     :common-name "Gold"
     :type :element/metal
     :density 19.300
     :radioactive false}

    {:crux.db/id :commodity/C
     :common-name "Carbon"
     :type :element/non-metal
     :density 2.267
     :radioactive false}

    {:crux.db/id :commodity/borax
     :common-name "Borax"
     :IUPAC-name "Sodium tetraborate decahydrate"
     :other-names ["Borax decahydrate" "sodium borate" "sodium tetraborate" "disodium tetraborate"]
     :type :mineral/solid
     :appearance "white solid"
     :density 1.73
     :radioactive false}])

  (crux/q (crux/db node)
          '{:find [element]
            :where [[element :type :element/metal]]})

  (.close node)

  ;;
  )