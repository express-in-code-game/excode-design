(ns starnet.app.alpha.crux
  (:require
   [clojure.repl :refer [doc]]
   [crux.api :as crux]
   [clojure.java.io :as io]))

(comment

  (def node (crux/start-node {:crux.node/topology '[crux.kafka/topology
                                                    crux.kv.rocksdb/kv-store]
                              :crux.kafka/bootstrap-servers "broker1:9092"
                              :crux.kafka/tx-topic "crux-transaction-log"
                              :crux.kafka/doc-topic "crux-docs"
                              :crux.kafka/create-topics true
                              :crux.kafka/doc-partitions 1
                              :crux.kafka/replication-factor 2
                              :crux.kv/db-dir "/tmp/crux"
                              :crux.kv/sync? false
                              :crux.kv/check-and-store-index-version true}))

  (.close node)

  ;;
  )