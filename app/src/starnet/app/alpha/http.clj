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
   [io.pedestal.http :as http]
   [io.pedestal.http.cors :as cors]
   [io.pedestal.interceptor :refer [interceptor]]
   [io.pedestal.http :as server]
   [io.pedestal.test :as test :refer [response-for raw-response-for]]
   [io.pedestal.http.content-negotiation :as conneg]
   [io.pedestal.test :as test]
   [buddy.hashers :as hashers]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check.generators :as gen]
   [starnet.app.alpha.core :as app.core])
  (:import
   [org.eclipse.jetty.websocket.api Session]))

(defn response [status body & {:as headers}]
  {:status status :body body :headers headers})

(def ok       (partial response 200))
(def created  (partial response 201))
(def accepted (partial response 202))


(def user-create
  {:name :user-create
   :leave
   (fn [ctx]
     (go
       (let [headers (get-in ctx [:request :headers])
             user-data (get-in ctx [:request :edn-params])
             channels (:channels ctx)]
         (let [hashed (hashers/derive (:u/password user-data) {:alg :bcrypt+sha512 :iterations 4})
               user-data2 (assoc user-data :u/password hashed)
               o (<! (app.core/create-user channels user-data2))]
           (if o
             (assoc ctx :response (ok o))
             (throw (ex-info "app.core/create-user failed" {:user-data user-data})))))))})

(def user-login
  {:name :user-create
   :leave
   (fn [ctx]
     (go
       (let [headers (get-in ctx [:request :headers])
             data (get-in ctx [:request :edn-params])
             channels (:channels ctx)]
         (let [user-record (<! (app.core/user-by-username channels (:u/username data)))
               raw (:u/password data)
               hashed (:u/password user-record)
               pass-valid? (hashers/check raw hashed)]
           (if pass-valid?
             (assoc ctx :response (ok true))
             (throw (ex-info "password invalid failed" {:user-record user-record})))))))})


(def user-delete
  {:name :user-delete
   :leave
   (fn [ctx]
     (go
       (let [headers (get-in ctx [:request :headers])
             user-data (get-in ctx [:request :edn-params])
             channels (:channels ctx)]
         (let [o (<! (app.core/evict-user channels (:u/uuid user-data)))]
           (if o
             (assoc ctx :response (ok o))
             (throw (ex-info "app.core/evict-user failed" {:user-data user-data})))))))})

(def user-list
  {:name :user-list
   :leave
   (fn [ctx]
     (go
       (let [headers (get-in ctx [:request :headers])
             body (get-in ctx [:request :body])]
         (println "123")
         (println (:channels ctx))
         (println headers)
         (println body))
       (assoc ctx :response (ok "list"))))})


(defn routes
  []
  (route/expand-routes
   #{["/user" :get [user-list]]
     ["/user" :post [(body-params) user-create]]
     ["/user" :delete [(body-params) user-delete]]}))


(comment

  ;https://github.com/pedestal/pedestal/blob/master/service/src/io/pedestal/test.clj
  ;http://pedestal.io/reference/parameters#_body_parameters

  (cors/allow-origin [])

  (def channels @(resolve 'starnet.app.alpha.main/channels))

  (def service (::http/service-fn (http/create-servlet (service-config channels (routes)))))

  (response-for service :get "/user")
  (response-for service :post "/user"
                :body (gen/generate (s/gen :u/user))
                :headers {"Content-Type" "application/edn"})

  (response-for service :delete "/user"
                :body (str (-> (app.core/repl-users channels) (rand-nth)))
                :headers {"Content-Type" "application/edn"})

  (count (app.core/repl-users channels))
  (def user (-> (app.core/repl-users channels) (rand-nth)))


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

(def supported-types ["text/html" "application/edn"  "text/plain" "application/transit+json"])

(def content-neg-intc (conneg/negotiate-content supported-types))

(defn i-channels
  [channels]
  (interceptor
   {:name :channels-interceptor
    :error nil
    :enter
    (fn [context]
      (assoc context :channels channels))
    :leave
    (fn [context]
      context)}))

(defn create-app-default-interceptors
  [channels]
  (fn [service-map]
    (update-in service-map [::http/interceptors]
               #(vec (-> %
                         (conj (i-channels channels))
                         (conj content-neg-intc))))))


;; http://pedestal.io/reference/service-map
(defn service-config
  [channels routes]
  (let [app-default-interceptors (create-app-default-interceptors channels)
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
  [channels]
  (let [service (service-config channels (routes))
        host (::http/host service)
        port (::http/port service)
        ssl-port (get-in service [::http/container-options :ssl-port])]
    (println (str "; starting http server on " host ":" port))
    (when ssl-port
      (println (str "; starting https server on " host ":" ssl-port)))
    (-> service
        (server/create-server)
        (server/start))))


