(ns starnet.app.alpha.core
  (:require
   [clojure.repl :refer [doc]]
   [clojure.core.async :as a :refer [<! >! <!! timeout chan alt! go
                                     >!! <!! alt!! alts! alts!! take! put!
                                     thread pub sub]]
   [clojure.java.io :as io]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [starnet.common.alpha.spec]))

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
  [channels data]
  (db-tx channels [[:crux.tx/put
                    (-> data
                        (merge {:crux.db/id (:u/uuid data)}))
                    ;; #inst "2112-12-03"
                    ;; #inst "2113-12-03"
                    ]]))
(defn evict-user
  [channels u-uuid]
  (db-tx channels [[:crux.tx/evict
                    u-uuid]]))

(defn repl-query
  [channels query-data]
  (let [c-out (chan 1)]
    (put! (channels :ch-cruxdb) {:cruxdb/op :query
                                 :ch/c-out c-out
                                 :cruxdb/query-data query-data})
    (first (alts!! [c-out (timeout 100)]))))

(defn repl-tx
  [channels tx-data]
  (let [c-out (chan 1)]
    (put! (channels :ch-cruxdb) {:cruxdb/op :tx
                                 :ch/c-out c-out
                                 :cruxdb/tx-data tx-data})
    (first (alts!! [c-out (timeout 100)]))))

(defn <!!soft
  [c]
  (first (alts!! [c (timeout 100)])))

(comment

  (def channels @(resolve 'starnet.app.alpha.main/channels))

  ; all entities

  (repl-query channels '{:find [id]
                         :where [[e :crux.db/id id]]
                         :full-results? true})

  (->
   (create-user channels (gen/generate (s/gen :u/user)))
   (<!!soft))

  (def users (->
              (repl-query channels '{:find [id]
                                     :where [[e :u/uuid id]]
                                     :full-results? true})

              (vec)
              (flatten)
              (vec)))

  (->
   (evict-user channels (-> users (nth 0) :u/uuid))
   (<!!soft))


  (let [c-out (chan 1)]
    (put! (channels :ch-access-store) {:kstore/op :create
                                       :kafka/k ""
                                       :kafka/ev {:access.token/token (.toString (java.util.UUID/randomUUID))
                                                  :access.token/inst-create (java.util.Date.)}
                                       :ch/c-out c-out})
    (first (alts!! [c-out (timeout 100)])))

  

  ;;
  )
