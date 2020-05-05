(ns cljctools.dgraph.client.protocols
  (:refer-clojure :exclude [alter]))

(defprotocol DgraphClient
  "Opts may contain :timeout 3000,then nil is retruned"
  (query [_ opts])
  (mutate [_ opts])
  (alter [_ opts])
  (upsert [_ opts])
  (drop-all [_] [_ opts]))

(defprotocol Connection
  (connect [_])
  (disconnect [_])
  (connected? [_]))

(defprotocol Release
  (release [_]))