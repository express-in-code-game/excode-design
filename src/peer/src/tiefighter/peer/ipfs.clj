(ns tiefighter.peer.ipfs
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >! <!! >!!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.string]
   [clojure.spec.alpha :as s]
   [clojure.java.io :as io]
   [clj-http.client :as clj-http.client]
   [jsonista.core]))

(def base-url "http://ipfs:5001")

(s/def ::pubsub-topic string?)

(defn version
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

(defn pubsub-sub
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