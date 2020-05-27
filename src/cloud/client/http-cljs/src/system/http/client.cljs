(ns system.http.client
  (:require
   [clojure.core.async :as a :refer [<! >!  timeout chan alt! go
                                     alts!  take! put! mult tap untap
                                     pub sub sliding-buffer mix admix unmix]]
   [cljs-http.client]))


(def ^:dynamic *default-opts* {:interceptors (fn
                                               ([] [identity])
                                               ([opts] [identity]))})

#_(defn- wrap-demo
    "wrap is bad design"
    [client]
    (fn [req]
      (client (-> req
                  (assoc-in [:headers "authorization"] "123")))))

(defn- wrap-interceptors
  "wrap is bad design"
  [client]
  (fn [req]
    (client (reduce (fn [ag f] (f ag)) req *interceptors*))))

(defprotocol HttpClient
  :extend-via-metadata true
  (request* [_ req]))

(defn create
  [opts]
  (let [{:keys [interceptors]} (merge *default-opts* opts)
        cljs-http-request (fn [req]
                            (->
                             (reduce (fn [ag f] (f ag)) req (interceptors *default-opts*))
                             (cljs-http.client/request)))]
    (with-meta
      {}
      {'HttpClient '_
       `request* (fn [_ req] (cljs-http-request req))})))

(def client (create {}))

(defn request
  ([req]
   (request* client req))
  ([client req]
   (request* client req)))

(defn auto-acquire
  []
  (request {:method :get}))

(defn auto-release
  []
  (request {:method :get}))

(defn create-game
  []
  (request {:method :post}))
