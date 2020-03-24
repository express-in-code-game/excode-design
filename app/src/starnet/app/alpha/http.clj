(ns starnet.app.alpha.http
  (:require
   [clojure.repl :refer [doc]]
   [clojure.core.async :as a :refer [<! >! <!! timeout chan alt! go
                                     >!! <!! alt!! alts! alts!! take! put!
                                     thread pub sub sliding-buffer mix admix unmix]]
   [io.pedestal.log :as log]
   [io.pedestal.http.route :as route]
   [io.pedestal.http.body-params :refer [body-params]]
   [io.pedestal.http.route.definition :refer [defroutes]]
   [ring.util.response :as ring-resp]
   [clojure.core.async :as async]
   [io.pedestal.http.jetty.websockets :as ws]
   [gniazdo.core :as wc]
   [io.pedestal.http :as http]
   [io.pedestal.http.cors :as cors]
   [io.pedestal.interceptor :refer [interceptor]]
   [io.pedestal.http :as server]
   [io.pedestal.test :as test :refer [response-for raw-response-for]]
   [io.pedestal.http.content-negotiation :as conneg]
   [io.pedestal.test :as test]
   [buddy.auth :as auth]
   [clj-time.core :as time]
   [buddy.hashers :as hashers]
   [buddy.auth.backends.token :refer [jwe-backend]]
   [buddy.auth.middleware :as auth.middleware]
   [io.pedestal.interceptor.chain :as interceptor.chain]
   [io.pedestal.interceptor.error :refer [error-dispatch]]
   [buddy.core.keys :as keys]
   [buddy.sign.jwt :as jwt]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check.generators :as gen]
   [starnet.app.alpha.core :as app.core])
  (:import
   [org.eclipse.jetty.websocket.api Session]
   java.net.URI))


(defn response [status body & {:as headers}]
  {:status status :body body :headers headers})

(def ok       (partial response 200))
(def created  (partial response 201))
(def accepted (partial response 202))
(def r403 (partial response 403))

(defn home
  [request]
  (if-not (auth/authenticated? request)
    (auth/throw-unauthorized)
    (ok {:status "Logged" :message (str "hello logged user "
                                        (:identity request))})))

(def authentication-interceptor
  (interceptor
   {:name ::authenticate
    :enter (fn [ctx]
             (let [backend (get-in ctx [:app/ctx :backend])
                   request (-> (:request ctx) (auth.middleware/authentication-request backend))]
               #_(println request)
               (println (format "auth/authenticated? %s" (auth/authenticated? request)))
               (update ctx :request  request)
               (if-not (auth/authenticated? request)
                 (auth/throw-unauthorized)
                 (assoc ctx :request request))))}))

(def authorization-interceptor
  (error-dispatch [ctx ex]
                  [{:exception-type :clojure.lang.ExceptionInfo :stage :enter}]
                  (try
                    (assoc ctx
                           :response
                           (auth.middleware/authorization-error (:request ctx)
                                                                ex
                                                                (get-in ctx [:app/ctx :backend])))
                    (catch Exception e
                      (assoc ctx ::interceptor.chain/error e)))

                  :else (assoc ctx ::interceptor.chain/error ex)))

(def user-create
  {:name :user-create
   :leave
   (fn [ctx]
     (go
       (let [headers (get-in ctx [:request :headers])
             user-data (get-in ctx [:request :edn-params])
             channels (get-in ctx [:app/ctx :channels])
             o (<! (app.core/create-user channels user-data))]
         (if o
           ctx
           (throw (ex-info "app.core/create-user failed" {:user-data user-data}))))))})


(def user-delete
  {:name :user-delete
   :leave
   (fn [ctx]
     (go
       (let [headers (get-in ctx [:request :headers])
             user-data (get-in ctx [:request :edn-params])
             channels (get-in ctx [:app/ctx :channels])]
         (let [o (<! (app.core/evict-user channels user-data))]
           (if o
             (assoc ctx :response (ok o))
             (throw (ex-info "app.core/evict-user failed" user-data)))))))})

(def user-list
  {:name :user-list
   :leave
   (fn [ctx]
     (go
       (let [headers (get-in ctx [:request :headers])
             d (get-in ctx [:request :edn-params])]
         (println "; user-list1")
         (println (get-in ctx [:app/ctx :backend]))
         (println (get-in ctx [:request :identity]))
         (println d)
         (assoc ctx :response (ok "list")))
       ))})

(def common-interceptors [(body-params)
                          http/html-body
                          authentication-interceptor
                          ])
(def user-login
  {:name :user-login
   :leave
   (fn [ctx]
     (go
       (let [headers (get-in ctx [:request :headers])
             channels (get-in ctx [:app/ctx :channels])
             pubkey (get-in ctx [:app/ctx :pubkey])
             data (get-in ctx [:request :edn-params])
             user (<! (app.core/user-by-username channels data))
             raw (or (:u/password-TMP data) (:u/password data))
             valid? (and user (hashers/check raw (:u/password user)))]
         (if valid?
           (let [claims {:val (select-keys data [:u/uuid])
                         :exp (time/plus (time/now) (time/seconds 3600))}
                 token (jwt/encrypt claims
                                    pubkey
                                    {:alg :rsa-oaep
                                     :enc :a128cbc-hs256})]
             (println (format "/login token count %s" (count token)))
             (println claims)
             (println data)
             (assoc ctx :response {:status 200
                                   :body (select-keys data [:u/uuid])
                                   :headers {"Authorization" (format "Token %s" token)}}))
           (assoc ctx :response (r403 "Invalid credentials"))))))})

(def user-settings
  {:name :get/settings
   :leave
   (fn [ctx]
     (go
       (let [headers (get-in ctx [:request :headers])
             channels (get-in ctx [:app/ctx :channels])
             claims (get-in ctx [:request :identity])
             user {} #_(<! (app.core/user-by-uuid channels (:val claims)))]
         (println claims)
         (assoc ctx :response {:status 200
                               :body (-> claims
                                         (dissoc :u/password :u/password-TMP))}))))})

(defn routes
  []
  (route/expand-routes
   #{["/user" :get (conj common-interceptors user-list) :route-name :get/user]
     ["/user" :post (conj (body-params) user-create user-login) :route-name :post/user]
     ["/user" :delete (conj common-interceptors user-delete) :route-name :delete/user]
     ["/login" :post (conj (body-params) user-login) :route-name :post/login]
     ["/settings" :get (conj common-interceptors user-settings) :route-name :get/settings]
     }))

(comment

  ;https://github.com/pedestal/pedestal/blob/master/service/src/io/pedestal/test.clj
  ;http://pedestal.io/reference/parameters#_body_parameters

  (def channels @(resolve 'starnet.app.alpha.main/channels))
  (def app-ctx {:channels channels
                :privkey (keys/private-key "resources/privkey.pem" (slurp "resources/passphrase.tmp"))
                :pubkey (keys/public-key "resources/pubkey.pem")})

  (def service (::http/service-fn (http/create-servlet (service-config app-ctx (routes)))))

  (def token (jwt/encrypt {:user {:val {:u/uuid (gen/generate gen/uuid)}}
                           :exp (time/plus (time/now) (time/seconds 3600))} (:pubkey app-ctx)
                          {:alg :rsa-oaep
                           :enc :a128cbc-hs256}))
  (count token)
  (def decrypted-data (jwt/decrypt token (:privkey app-ctx)
                                   {:alg :rsa-oaep
                                    :enc :a128cbc-hs256}))

  (count (app.core/repl-users channels))
  (-> (app.core/repl-users channels) (rand-nth))

  (response-for service :get "/user" :headers {"Authorization" (format "Token %s" token)})

  (response-for service :post "/user"
                :body (str (gen/generate (s/gen :u/user)))
                :headers {"Content-Type" "application/edn"})

  (response-for service :delete "/user"
                :body (str (-> (app.core/repl-users channels) (rand-nth)))
                :headers {"Content-Type" "application/edn"})

  ; valid
  (response-for service :post "/login"
                :body (str (-> (app.core/repl-users channels) (rand-nth)))
                :headers {"Content-Type" "application/edn"})

  ; invalid
  (response-for service :post "/login"
                :body (str {:u/username "mock" :u/password "mock"})
                :headers {"Content-Type" "application/edn"})


  (def user (-> (app.core/repl-users channels) (rand-nth)))
  
  (def token
    (->
     (response-for service :post "/login"
                   :body (str user)
                   :headers {"Content-Type" "application/edn"})
     (get-in [:headers "Authorization"])
     (clojure.string/split #" ")
     (second)))
  
  (count token)

  (response-for service :get "/settings"
                :headers {"Content-Type" "application/edn"
                          "Authorization" (format "Token %s" token)})

  (response-for service :get "/settings"
                :headers {"Content-Type" "application/edn"})

  (->
   (app.core/user-by-username channels (-> (app.core/repl-users channels) (rand-nth)))
   (app.core/<!!soft))


  ;;
  )


(def ws-clients (atom {}))

(defn new-ws-client
  [ws-session send-ch]
  (async/put! send-ch "This will be a text message")
  (swap! ws-clients assoc ws-session send-ch))

;; This is just for demo purposes
(defn send-and-close! []
  (let [[ws-session send-ch] (first @ws-clients)]
    (async/put! send-ch "A message from the server")
    ;; And now let's close it down...
    (async/close! send-ch)
    ;; And now clean up
    (swap! ws-clients dissoc ws-session)))

;; Also for demo purposes...
(defn send-message-to-all!
  [message]
  (doseq [[^Session session channel] @ws-clients]
    ;; The Pedestal Websocket API performs all defenmake-service-fullsive checks before sending,
    ;;  like `.isOpen`, but this example shows you can make calls directly on
    ;;  on the Session object if you need to
    (when (.isOpen session)
      (async/put! channel message))))

(def ws-paths
  {"/ws" {:on-connect (ws/start-ws-connection new-ws-client)
          :on-text (fn [msg] (log/info :msg (str "A client sent - " msg)))
          :on-binary (fn [payload offset length] (log/info :msg "Binary Message!" :bytes payload))
          :on-error (fn [t] (log/error :msg "WS Error happened" :exception t))
          :on-close (fn [num-code reason-text]
                      (log/info :msg "WS Closed:" :reason reason-text))}})

(comment

  (def uri "ws://0.0.0.0:8080/ws")
  
  (def cl (wc/client (URI. uri)))
  (.start cl)
  (.stop cl)
  
  (def socket
    (wc/connect
     uri
     :client cl
     :on-receive #(prn 'received %)))
  (wc/send-msg socket "hello")
  (wc/close socket)

  ;;
  )


(def supported-types ["text/html" "application/edn"  "text/plain" "application/transit+json"])

(def content-neg-intc (conneg/negotiate-content supported-types))

(defn app-ctx-interceptor
  [app-ctx]
  (interceptor
   {:name :app-ctx-interceptor
    :error nil
    :enter
    (fn [context]
      (let [privkey (get-in app-ctx [:privkey])
            backend (jwe-backend {:secret privkey
                                  :options {:alg :rsa-oaep
                                            :enc :a128cbc-hs256}
                                  :authfn (fn [claims]
                                            (-> claims
                                                (update :val
                                                        (fn [o]
                                                          (->> o
                                                               (map (fn [[k v]]
                                                                      (if (= (name k) "uuid")
                                                                        [k (java.util.UUID/fromString v)]
                                                                        [k v])))
                                                               (into {}))))
                                                (assoc :identity true)))})
            app-ctx (assoc app-ctx :backend backend)]
        (assoc context :app/ctx app-ctx)))
    :leave
    (fn [context]
      context)}))

(defn create-app-default-interceptors
  [app-ctx]
  (fn [service-map]
    (update-in service-map [::http/interceptors]
               #(vec (-> %
                         (conj (app-ctx-interceptor app-ctx))
                         (conj content-neg-intc))))))

;; http://pedestal.io/reference/service-map
(defn service-config
  [app-ctx routes]
  (let [app-default-interceptors (create-app-default-interceptors app-ctx)
        port 8080
        port-ssl 8443
        host "0.0.0.0"]
    (->
     {:env :prod
      ::http/routes routes
      ::http/resource-path "/public"
      ::http/type :jetty
      ::http/container-options {:context-configurator #(ws/add-ws-endpoints % ws-paths)
                                ; :h2c? true
                                ; :h2? true
                                :ssl? true
                                :ssl-port port-ssl
                                :keystore "resources/keystore.jks"
                                :key-password "keystore"}
      ::http/host host
      ::http/port port}
     (merge {:env :dev
             ::http/join? false
             ::http/allowed-origins {:creds true :allowed-origins (constantly true)}})
     http/default-interceptors
     http/dev-interceptors
     app-default-interceptors)))

(defn start
  [app-ctx]
  (let [service (service-config app-ctx (routes))
        host (::http/host service)
        port (::http/port service)
        ssl-port (get-in service [::http/container-options :ssl-port])]
    (println (str "; starting http server on " host ":" port))
    (when ssl-port
      (println (str "; starting https server on " host ":" ssl-port)))
    (-> service
        (server/create-server)
        (server/start))))

(defn stop
  [service-map]
  (server/stop service-map))
