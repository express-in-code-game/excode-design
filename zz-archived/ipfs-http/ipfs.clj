(ns find.app.ipfs
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >! <!! >!!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.string]
   [clojure.pprint :refer [pprint]]
   [clojure.spec.alpha :as s]
   [clojure.java.io :as io]
   [clj-http.client :as clj-http.client]
   [jsonista.core]))

(def base-url "http://ipfs:5001")

(s/def ::pubsub-topic string?)

(defn api-version
  []
  (go
    (let [response
          (->
           (clj-http.client/request
            {:url (str base-url "/api/v0/version")
             :method :post
             :headers {:content-type "application/json"}})
           :body
           (jsonista.core/read-value jsonista.core/keyword-keys-object-mapper))]
      response)))

(defn api-pubsub-sub
  [{:keys [::pubsub-topic] :or {pubsub-topic "123"}}]
  (go
    (with-open [stream (->
                        (clj-http.client/request
                         {:url (str base-url "/api/v0/pubsub/sub")
                          :method :post
                          :as :stream
                          :query-params {"arg" pubsub-topic}})
                        :body)]
      (let [lines (-> stream io/reader line-seq)]
        (doseq [line lines]
          (println :line
                   (jsonista.core/read-value line jsonista.core/keyword-keys-object-mapper)))))))

(defn api-swarm-peers
  []
  (go
    (let [response
          (->
           (clj-http.client/request
            {:url (str base-url "/api/v0/swarm/peers")
             :method :post
             :headers {:content-type "application/json"}
             :query-params {"verbose" true
                            "streams" true
                            "latency" true
                            "direction" true}})
           :body
           (jsonista.core/read-value jsonista.core/keyword-keys-object-mapper))]
      (->>
       response
       :Peers
       #_(map :Peer)
       rand-nth
       #_count
       #_pprint))))

(defn api-find-peer
  []
  (go
    (let [peer-id (:Peer (<! (api-swarm-peers)))
          response
          (->
           (clj-http.client/request
            {:url (str base-url "/api/v0/dht/findpeer")
             :method :post
             :headers {:content-type "application/json"}
             :query-params {"arg" peer-id
                            "verbose" true}})
           :body
           (jsonista.core/read-value jsonista.core/keyword-keys-object-mapper))]
      response)))

(defn api-name-resolve
  []
  (go
    (let [peer-id (:Peer (<! (api-swarm-peers)))
          response
          (->
           (clj-http.client/request
            {:url (str base-url "/api/v0/name/resolve")
             :method :post
             :headers {:content-type "application/json"}
             :query-params {"arg" peer-id
                            "dht-record-count" 10
                            "dht-timeout" "30s"}})
           :body
           (jsonista.core/read-value jsonista.core/keyword-keys-object-mapper))]
      response)))


(defn crawl
  []
  
  )