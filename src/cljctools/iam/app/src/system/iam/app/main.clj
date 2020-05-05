(ns system.iam.app.main
  (:require
   [clojure.core.async :as a :refer [<! >! <!! timeout chan alt! go close!
                                     >!! <!! alt!! alts! alts!! take! put! mult tap untap
                                     thread pub sub sliding-buffer mix admix unmix]]
   [clojure.java.io :as io]
   [clojure.java.shell :refer [sh]]

   [io.pedestal.http.body-params :refer [body-params]]
   [io.pedestal.http :as http]
   [io.pedestal.http.route :as route]
   [io.pedestal.http.cors :as cors]

   [buddy.hashers :as hashers]
   [buddy.auth.backends.token :refer [jwe-backend]]
   [buddy.auth.middleware :as auth.middleware]
   [buddy.auth :as auth]
   [buddy.core.keys :as keys]
   [buddy.sign.jwt :as jwt]
   [clj-time.core :as time]

   [system.iam.spec]
   [system.dgraph.api :as api.db]))


(defn ensure-tls-keys
  []
  (let [script "
                bash f gen_ec
                bash f gen_rsa
                "]
    (go
      (if-not (and (.exists (io/file "resources/privkey.pem"))
                   (.exists (io/file "resources/pubkey.pem")))
        (do
          (println "; generating keys")
          (sh "bash" "-c" script :dir "/ctx/app"))
        (println "; keys exist")))))

(def authentication-interceptor
  {:name ::authenticate
   :enter (fn [ctx]
            (let [backend (get-in ctx [:app/ctx :backend])
                  request (-> (:request ctx) (auth.middleware/authentication-request backend))]
              #_(println request)
              (println (format "auth/authenticated? %s" (auth/authenticated? request)))
              (update ctx :request  request)
              (if-not (auth/authenticated? request)
                (auth/throw-unauthorized)
                (assoc ctx :request request))))})


(def common-interceptors [(body-params)
                          http/html-body
                          authentication-interceptor])


(def user-create
  {:name :user-create
   :enter
   (fn [ctx]
     (go
       (let [headers (get-in ctx [:request :headers])
             user-data (get-in ctx [:request :edn-params])
             channels (get-in ctx [:app/ctx :channels])
             [tx tx-data]  nil #_(<! (app.core/create-user channels user-data))]
         (if tx
           (assoc-in ctx [:request :u/user] tx-data)
           (throw (ex-info "app.core/create-user failed" {:user-data user-data}))))))})

(def user-delete
  {:name :user-delete
   :leave
   (fn [ctx]
     (go
       (let [headers (get-in ctx [:request :headers])
             user-data (get-in ctx [:request :edn-params])
             channels (get-in ctx [:app/ctx :channels])]
         (let [o nil #_(<! (app.core/evict-user channels user-data))]
           (if o
             (assoc ctx :response {:status 200})
             (throw (ex-info "app.core/evict-user failed" user-data)))))))})

(def user-login
  {:name :user-login
   :leave
   (fn [ctx]
     (go
       (let [headers (get-in ctx [:request :headers])
             channels (get-in ctx [:app/ctx :channels])
             pubkey (get-in ctx [:app/ctx :pubkey])
             data (get-in ctx [:request :edn-params])
             user (or (get-in ctx [:request :u/user]) nil #_(<! (app.core/user-by-username channels data)))
             raw (or (:u/password-TMP data) (:u/password data))
             valid? (and user (hashers/check raw (:u/password user)))]
         (if valid?
           (let [claims {:val (select-keys user [:u/uuid])
                         :exp (time/plus (time/now) (time/seconds (* 24 60 60)))}
                 token (jwt/encrypt claims
                                    pubkey
                                    {:alg :rsa-oaep
                                     :enc :a128cbc-hs256})]
             (assoc ctx :response {:status 200
                                   :body (select-keys user [:u/uuid :u/username :u/email
                                                            :u/fullname :u/links])
                                   :headers {"Authorization" (format "Token %s" token)}}))
           (assoc ctx :response {:status 403 :body "Invalid credentials"})))))})

(def user-get
  {:name :get/account
   :leave
   (fn [ctx]
     (go
       (let [headers (get-in ctx [:request :headers])
             channels (get-in ctx [:app/ctx :channels])
             claims (get-in ctx [:request :identity])
             user nil #_(<! (app.core/user-by-uuid channels (:val claims)))]
         (assoc ctx :response {:status 200
                               :body (select-keys user [:u/uuid :u/username :u/email
                                                        :u/fullname :u/links])}))))})

(defn routes
  []
  (route/expand-routes
   #{["/user" :post [(body-params) user-create user-login] :route-name :user/post]
     ["/user" :delete (conj common-interceptors user-delete) :route-name :user/delete]
     ["/user" :get (conj common-interceptors user-get) :route-name :user/get]
     ["/login" :post [(body-params) user-login] :route-name :login/post]}))


(def port 8080)
(def host "0.0.0.0")

;; http://pedestal.io/reference/service-map
(defn service
  []
  (->
   {:env :prod
    ::http/routes routes
    ::http/resource-path "/public"
    ::http/type :jetty
    ::http/host host
    ::http/port port}
   (merge {:env :dev
           ::http/join? false
           ::http/allowed-origins {:creds true :allowed-origins (constantly true)}})
   http/default-interceptors
   http/dev-interceptors))

(defprotocol Startable
  (start [_])
  (stop [_])
  (restart [_]))

(defn create-server []
  (let [server (atom nil)]
    (reify
      Startable
      (start [_]
        (println (str "; starting http server on " host ":" port))
        (reset! server (-> (service)
                           (http/create-server)
                           (http/start))))
      (stop [_]
        (println (str "; stopping http server on " host ":" port))
        (http/stop server)
        (reset! server nil))
      (restart [_]
        (stop _)
        (start _)))))


(def channels (let [main| (chan 10)
                    ops| (chan 10)]
                {:main| main|
                 :ops| ops|}))

(def db-client (api.db/create-client {:connections [{:hostname "alpha"
                                                     :port 9080}]}))

(def server (create-server))
#_(-restart server)


(defn -main [& args]
  (<!! (ensure-tls-keys))
  (api.db/connect db-client)
  (start server))

(comment
  
  
  
  
  ;;
  )