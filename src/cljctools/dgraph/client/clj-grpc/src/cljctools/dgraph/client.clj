(ns cljctools.dgraph.client
  (:refer-clojure :exclude [alter])
  (:require
   [clojure.core.async :as a :refer [<! >! <!! timeout chan alt! go close!
                                     >!! <!! alt!! alts! alts!! take! put! mult tap untap
                                     thread pub sub sliding-buffer mix admix unmix]]
   [clojure.pprint :as pp]
   [clojure.reflect :as reflect]
   [clojure.data.json :as json]
   [cljctools.dgraph.client.protocols :as p])
  (:import
   io.grpc.ManagedChannel
   io.grpc.ManagedChannelBuilder
   io.grpc.Metadata
   io.grpc.Metadata$Key
   io.grpc.stub.MetadataUtils
   io.dgraph.DgraphClient
   io.dgraph.DgraphGrpc
   io.dgraph.Transaction
   io.dgraph.DgraphGrpc$DgraphStub
   io.dgraph.DgraphProto$Mutation
   io.dgraph.DgraphProto$Operation
   io.dgraph.DgraphProto$Response
   io.dgraph.DgraphProto$Request
   com.google.gson.Gson
   com.google.protobuf.ByteString))

(defn- json-write-key-fn
  [k]
  (cond (instance? clojure.lang.Keyword k) (str (symbol k))
        (instance? clojure.lang.Symbol k) (str k)
        (instance? clojure.lang.Named k) (name k)
        (nil? k) (throw (Exception. "JSON object properties may not be nil"))
        :else (str k)))

(def ^:dynamic *json-write-key-fn* json-write-key-fn)

(comment
  (*json-write-key-fn* :u/username) ; -> "u/username"
  (json/write-str {:a.b/c 3} :key-fn dg/*json-write-key-fn*)
  ;;
  )

(def default-options {})

(defn create-client
  [{:keys [connections] :as opts}]
  (let [ops| (chan 10)
        operation (fn [op opts]
                    (go
                      (if-not (clojure.core.async.impl.protocols/closed? ops|)
                        (let [out| (chan 1)]
                          (put! ops| {:op op :opts opts :out| out|})
                          (if (:timeout opts)
                            (alt!
                              out| ([v] v)
                              (timeout (:timeout opts)) nil)
                            (<! out|)))
                        false)))
        create-dgraph-client (fn []
                               (let [dg-channels (into []
                                                       (map (fn [{:keys [hostname port]}]
                                                              (->
                                                               (ManagedChannelBuilder/forAddress hostname port)
                                                               (.usePlaintext #_true)
                                                               (.build))))
                                                       connections)
                                     stubs (into []
                                                 (map (fn [ch]
                                                        (DgraphGrpc/newStub ch)))
                                                 dg-channels)]
                                 {:dg-channels  dg-channels
                                  :dg-client (DgraphClient. (into-array stubs))}))
        dgclient (atom nil)
        dgchannels (atom nil)
        connect (fn []
                  (let [{:keys [dg-channels dg-client]} (create-dgraph-client)]
                    (reset! dgclient dg-client)
                    (reset! dgchannels dg-channels)))
        disconnect (fn []
                     (doseq [ch @dgchannels]
                       (.shutdown ch))
                     (reset! dgclient nil)
                     (reset! dgchannels nil))
        dg-proto-response->edn (fn [res]
                                 (-> res
                                     (.getJson)
                                     (.toStringUtf8)
                                     (json/read-str :key-fn clojure.core/keyword))
                                 #_(with-open [baos (java.io.ByteArrayOutputStream.)]
                                     (.writeTo (.getJson res) baos)
                                     (.toString baos)
                                     (json/read-str :key-fn clojure.core/keyword)))
        lookup {:dgclient dgclient
                :opts opts}]
    (go (loop []
          (when-let [{:keys [op opts out|]} (<! ops|)]
            (condp = op
              :query (future (let [{:keys [query vars]} opts
                                   res (-> @dgclient
                                           (.newTransaction)
                                           (.queryWithVars query vars)
                                           (dg-proto-response->edn))]
                               (put! out| res)
                               (close! out|)))
              :mutate (future (let [{:keys [data]} opts
                                    res (let [txn (.newTransaction @dgclient)]
                                          (try
                                            (let [mu  (->
                                                       (DgraphProto$Mutation/newBuilder)
                                                       (.setSetJson (ByteString/copyFromUtf8 (json/write-str data
                                                                                                             :key-fn *json-write-key-fn*)))
                                                       #_(.setSetNquads (ByteString/copyFromUtf8 data))
                                                       (.build))]
                                              (.mutate txn mu)
                                              (.commit txn))
                                            (catch Exception e (str "dgrah client :mutate err: " (.getMessage e)))
                                            (finally (.discard txn))))
                                    res (dg-proto-response->edn res)]
                                (put! out| res)
                                (close! out|)))
              :upsert (future (let [{:keys [query mutations]} opts
                                    res (let [txn (.newTransaction @dgclient)]
                                          (try
                                            (let [ms (map
                                                      (fn [{:keys [data condition]}]
                                                        (let [b (->
                                                                 (DgraphProto$Mutation/newBuilder)
                                                                 (.setSetJson (ByteString/copyFromUtf8 (json/write-str data
                                                                                                                       :key-fn *json-write-key-fn*)))
                                                                 #_(.setSetNquads (ByteString/copyFromUtf8 mutation)))]
                                                          (when condition (.setCond b condition))
                                                          (.build b))) mutations)
                                                  req (->
                                                       (DgraphProto$Request/newBuilder)
                                                       (.setQuery query)
                                                       (.addMutations (first ms))
                                                       (.setCommitNow true)
                                                       (.build))]
                                              (.doRequest txn req))
                                            (catch Exception ex (do
                                                                  (str "dgrah client :upsert err: " (.getMessage ex))
                                                                  (prn ex)))
                                            (finally (.discard txn))))
                                    res (dg-proto-response->edn res)]
                                (put! out| res)
                                (close! out|)))
              :alter (future (let [{:keys [schema]} opts
                                   op (->
                                       (DgraphProto$Operation/newBuilder)
                                       (.setSchema schema)
                                       (.build))]
                               (.alter @dgclient op)
                               (put! out| true)
                               (close! out|)))
              :drop-all (future (let [op (->
                                          (DgraphProto$Operation/newBuilder)
                                          (.setDropAll true)
                                          (.build))]
                                  (.alter @dgclient op)
                                  (put! out| true)
                                  (close! out|))))
            (recur))))
    (reify
      p/DgraphClient
      (query [_ opts]
        (operation :query opts))
      (mutate [_ opts]
        (operation :mutate opts))
      (alter [_ opts]
        (operation :alter opts))
      (upsert [_ opts]
        (operation :upsert opts))
      (drop-all [_]
        (operation :drop-all {}))
      p/Release
      (release [_] (do (close! ops|)
                       (disconnect)))
      p/Connection
      (connect [_] (connect) true)
      (disconnect [_] (disconnect) true)
      (connected? [_] (some? @dgclient))
      clojure.lang.ILookup
      (valAt [_ k] (.valAt _ k nil))
      (valAt [_ k not-found] (.valAt lookup k not-found)))))

(defn connect [client]
  (p/connect client))

(defn release [client]
  (p/release client))

(defn query
  [client opts]
  (p/query client opts))

(defn mutate
  [client opts]
  (p/mutate client opts))

(defn upsert
  [client opts]
  (p/upsert client opts))

(defn alter
  [client opts]
  (p/alter client opts))

(defn drop-all
  [client]
  (p/drop-all client))

(defn q-schema
  [client]
  (p/query client {:query "
                             schema {}
                             "
                   :vars {}}))

(defn q-count-attr
  [client attr]
  (p/query client {:query (format
                           "
                             {
      total (func: has (<%s>) ) {
      count(uid)
      }
     } 
                             "
                           attr
                           )
                   :vars {}}))

(defn linst-methods
  [v]
  (->> v
       reflect/reflect
       :members
       (filter #(contains? (:flags %) :public))
       (filter #(or (instance? clojure.reflect.Method %)
                    (instance? clojure.reflect.Constructor %)))
       (sort-by :name)
       (map #(select-keys % [:name :return-type :parameter-types]))
       pp/print-table))

(defn linst-fields
  [v]
  (->> v
       reflect/reflect
       :members
       (filter #(contains? (:flags %) :public))
       (filter #(not (or (instance? clojure.reflect.Method %)
                         (instance? clojure.reflect.Constructor %))))
       (sort-by :name)
       (map #(select-keys % [:name :return-type :parameter-types]))
       pp/print-table))

(defn linst
  [v]
  (linst-fields v)
  (linst-methods v))

(comment

  (def req (->
            (DgraphProto$Request/newBuilder)
            (.setQuery "")
            (.setCommitNow true)
            (.build)))
  (def req-b (DgraphProto$Request/newBuilder))
  (def mu-b (DgraphProto$Mutation/newBuilder))


  (def cl (create-client {:connections [{:hostname "alpha"
                                         :port 9080}]}))
  (connect cl)

  (def data {:u/username "user1"
             :u/email "user1@gmail.com"
             :u/fullname "user one"
             :u/links []
             :u/password "asdasd"
             :u/password-TMP "pass"})

  (def res (first (alts!! [(create-user cl data)
                           (timeout 5000)])))

  (linst res)
  (.getJson res)
  (linst (.getJson res))
  (.toStringUtf8 (.getJson res))
  (linst (.getTxn res))
  (.toString (.getTxn res))
  (.toString (.getJson res))
  (.writeTo (.getJson res) (java.io.ByteArrayOutputStream.))
  (linst (java.io.ByteArrayOutputStream.))
  (.toString (java.io.ByteArrayOutputStream.))

  (with-open [baos (java.io.ByteArrayOutputStream.)]
    (.writeTo (.getJson res) baos)
    (.toString baos))

  (linst @(:dgclient cl)) ; .alter returns void
  (linst (.newTransaction @(:dgclient cl)))
  
  (def c| (chan 1))
  (close! c|)
  (clojure.core.async.impl.protocols/closed? c|)
  
  ;;
  )
