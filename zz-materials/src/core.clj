(ns starnet.alpha.core
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
   [buddy.hashers :as hashers]
   [starnet.alpha.core.spec]))


(defn db-tx
  [{:keys [ch-cruxdb] :as channels} tx-data]
  (let [c-out (chan 1)]
    (put! ch-cruxdb {:cruxdb/op :tx
                     :cruxdb/tx-data tx-data
                     :ch/c-out c-out})
    c-out))

(defn create-user
  [channels data]
  (go
    (let [password (:u/password data)
          hashed (hashers/derive password {:alg :bcrypt+sha512 :iterations 4})
          data (merge data  {:u/password hashed
                             :u/password-TMP password})
          tx-data (-> data
                      (merge {:crux.db/id (:u/uuid data)}))
          tx (<! (db-tx channels [[:crux.tx/put
                                   tx-data
                    ;; #inst "2112-12-03"
                    ;; #inst "2113-12-03"
                                   ]]))]
      [tx tx-data])))

(defn user-by-username
  [channels data]
  (let [{:keys [u/username]} data
        q {:find '[e]
           :where [['e :u/username username]]
           :full-results? true}]
    (go
      (let [c-out (chan 1)]
        (>! (channels :ch-cruxdb) {:cruxdb/op :query
                                   :ch/c-out c-out
                                   :cruxdb/query-data q})
        (let [o (<! c-out)]
          (ffirst o))))))

(defn user-by-uuid
  [channels data]
  (let [{:keys [u/uuid]} data
        q {:find '[e]
           :where [['e :u/uuid uuid]]
           :full-results? true}]
    (go
      (let [c-out (chan 1)]
        (>! (channels :ch-cruxdb) {:cruxdb/op :query
                                   :ch/c-out c-out
                                   :cruxdb/query-data q})
        (let [o (<! c-out)]
          (ffirst o))))))
   
(defn evict-user
  [channels user-data]
  (db-tx channels [[:crux.tx/evict
                    (:u/uuid user-data)]]))

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

(defn repl-users
  [channels]
  (->
   (repl-query channels '{:find [id]
                          :where [[e :u/uuid id]]
                          :full-results? true})

   (vec)
   (flatten)
   (vec)))



(comment

  (def channels @(resolve 'starnet.alpha.main/channels))

  ; all entities
  (repl-query channels '{:find [id]
                         :where [[e :crux.db/id id]]
                         :full-results? true})
  

  (dotimes [n 10]
    (->
     (create-user channels (gen/generate (s/gen :u/user)))
     (<!!soft)))

  (count (repl-users channels))
  (->> (repl-users channels) (take 5))
  

  (repl-query channels {:find '[e]
                        :where '[[e :crux.db/id id]]
                        :args [{'id (-> (repl-users channels) (rand-nth) :u/uuid)}]
                        :full-results? true})

  (def users (repl-users channels))

  (->
   (evict-user channels (-> users (rand-nth) :u/uuid))
   (<!!soft))

  (def user (-> (repl-users channels) (rand-nth)))

  (->
   (let [c-out (chan 1)]
     (put! (channels :ch-kstore-user) {:kstore/op :read-store
                                       :ch/c-out c-out})
     (first (alts!! [c-out (timeout 100)])))
   (count))


  (->
   (user-by-username channels (-> (repl-users channels) (rand-nth)))
   (<!!soft))

  ;;
  )
