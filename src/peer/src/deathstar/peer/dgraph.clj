(ns deathstar.peer.dgraph
  (:gen-class)
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >! <!! >!!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.string]
   [clojure.java.io]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   #_[clojure.spec.test.alpha :as stest]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]

   [clj-http.client]
   [jsonista.core]
   [deathstar.spec]))

(def base-url "http://alpha:8080")

(defn upload-schema
  []
  (go
    (println ::uploading-schema)
    (let [timeout| (timeout 20000)]
      (loop []
        (let [response| (go
                          (try
                            (let [response
                                  (->
                                   (clj-http.client/request
                                    {:url (str base-url "/admin/schema")
                                     :method :post
                                     :body (clojure.java.io/file (clojure.java.io/resource "dgraph/schema.gql"))})
                                   :body
                                   (jsonista.core/read-value jsonista.core/keyword-keys-object-mapper))]
                              (println :response response)
                              response)
                            (catch Exception e (println (ex-message e)))))]
          (alt!
            timeout| (do
                       (println ::uploading-schema-timed-out)
                       false)
            response| ([value]
                       #_(println value)
                       (if (= (-> value :data :code) "Success")
                         (do
                           (println ::uploaded-schema)
                           true)
                         (do
                           (<! (timeout 1000))
                           (recur))))))))))

(defn query-types
  []
  (go
    (let [response
          (->
           (clj-http.client/request
            {:url (str base-url "/graphql")
             :method :post
             :headers {:content-type "application/json"}
             :body (jsonista.core/write-value-as-string
                    {"query"  "
                                {__schema {types {name}}}
                                "
                     "variables" {}})})
           :body
           (jsonista.core/read-value jsonista.core/keyword-keys-object-mapper))]
      response)))

(defn query-users
  []
  (go
    (let [response
          (->
           (clj-http.client/request
            {:url (str base-url "/graphql")
             :method :post
             :headers {:content-type "application/json"}
             :body (jsonista.core/write-value-as-string
                    {"query"  (slurp (clojure.java.io/resource "dgraph/query-users.gql"))
                     "variables" {}})})
           :body
           (jsonista.core/read-value jsonista.core/keyword-keys-object-mapper))]
      response)))

(defn query-user
  [{:keys [:deathstar.spec/username] :as opts}]
  (go
    (let [response
          (->
           (clj-http.client/request
            {:url (str base-url "/graphql")
             :method :post
             :headers {:content-type "application/json"}
             :body (jsonista.core/write-value-as-string
                    {"query"  "
                                 queryUser () {
                                  username
                                 }
                                "
                     "variables" {}})})
           :body
           (jsonista.core/read-value jsonista.core/keyword-keys-object-mapper))]
      response)))

(defn add-random-user
  []
  (go
    (let [response
          (->
           (clj-http.client/request
            {:url (str base-url "/graphql")
             :method :post
             :headers {:content-type "application/json"}
             :body (jsonista.core/write-value-as-string
                    {"query"  (slurp (clojure.java.io/resource "dgraph/add-user.gql"))
                     "variables" {"user" {"username" (gen/generate (s/gen string?))
                                          "name" (gen/generate (s/gen string?))
                                          "password" (gen/generate (s/gen string?))}}})})
           :body
           (jsonista.core/read-value jsonista.core/keyword-keys-object-mapper))]
      response)))

(defn healthy?
  []
  (go
    (let [timeout| (timeout 10000)]
      (loop []
        (let [response|
              (go
                (try
                  (->
                   (clj-http.client/request
                    {:url (str base-url "/state")
                     :method :get
                     :headers {:content-type "application/json"}})
                   :body
                   (jsonista.core/read-value jsonista.core/keyword-keys-object-mapper)
                   keys)
                  (catch Exception e (do nil))))]
          (alt!
            timeout| false
            response| ([value]
                       (if (= (-> value first :status) "healthy") ; outdated
                         (do
                           (println value)
                           true)
                         (do
                           (println value)
                           (<! (timeout 1000))
                           (recur))))))))))

(defn down?
  [])