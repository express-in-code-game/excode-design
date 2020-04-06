(ns starnet.alpha.main
  (:require
   [clojure.repl :refer [doc]]
   [reagent.core :as r]
   [clojure.core.async :as a :refer [<! >!  timeout chan alt! go
                                     alts!  take! put! mult tap untap
                                     pub sub sliding-buffer mix admix unmix]]
   [cljs-http.client :as http]
   [goog.string :as gstring]
   [goog.string.format]
   [cognitect.transit :as transit]

   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check :as tc]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]

   [bidi.bidi :as bidi]
   [pushy.core :as pushy]
   [datascript.core :as ds]

   [starnet.pad.datascript1]
   [starnet.alpha.core.spec]
   [starnet.alpha.tests]
   [starnet.alpha.render :as render]
   [starnet.pad.async2]
   #_[starnet.alpha.core.game.store :as game])
  (:import [goog.net XhrIo EventType WebSocket]
           [goog Uri]
           goog.history.Html5History))

(declare proc-main proc-socket proc-render-containers proc-db proc-ops proc-http
         proc-history proc-router proc-derived-state proc-render-ui proc-worker)

(enable-console-print!)

(defonce channels (let [ch-proc-main (chan 1)
                        ch-sys (chan (sliding-buffer 10))
                        pb-sys (pub ch-sys :ch/topic (fn [_] (sliding-buffer 10)))
                        ch-socket-in (chan (sliding-buffer 100))
                        ch-socket-out (chan (sliding-buffer 100))
                        ch-history (chan (sliding-buffer 10))
                        ch-router (chan (sliding-buffer 10))
                        ml-router (mult ch-router)
                        ch-history-states (chan (sliding-buffer 10))
                        ml-history-states (mult ch-history-states)
                        ch-db (chan (sliding-buffer 10))
                        ch-http (chan (sliding-buffer 10))
                        ch-ops (chan (sliding-buffer 100))
                        ch-inputs (chan (sliding-buffer 100))
                        pb-inputs (pub ch-inputs :ch/topic (fn [_] (sliding-buffer 100)))]
                    {:ch-proc-main ch-proc-main
                     :ch-sys ch-sys
                     :pb-sys pb-sys
                     :ch-db ch-db
                     :ch-history ch-history
                     :ch-router ch-router
                     :ml-router ml-router
                     :ch-history-states ch-history-states
                     :ml-history-states ml-history-states
                     :ch-http ch-http
                     :ch-ops ch-ops
                     :ch-inputs ch-inputs
                     :pb-inputs pb-inputs
                     :ch-socket-in ch-socket-in
                     :ch-socket-out ch-socket-out
                    ;;  :game-channels (game/make-channels)
                     }))

(defn ^:export main
  []
  (put! (channels :ch-proc-main) {:proc/op :start})
  (proc-main (select-keys channels [:ch-proc-main :ch-sys])))


(defn proc-main
  [{:keys [ch-proc-main ch-sys]}]
  (go (loop []
        (when-let [{op :proc/op} (<! ch-proc-main)]
          (println (gstring/format "proc-main %s" op))
          (condp = op
            :start (do
                     (proc-socket (select-keys channels [:pb-sys :ch-sys :ch-socket-in :ch-socket-out]))
                     (proc-history (select-keys channels [:pb-sys :ch-sys :ch-history :ch-history-states]))
                     (proc-router (select-keys channels [:ch-sys :ch-history :ml-history-states :ch-router]))
                     (proc-db (select-keys channels [:pb-sys :ch-db]))
                     (proc-derived-state (select-keys channels [:ml-router :ml-http-res :ch-db]))
                     (proc-http (select-keys channels [:ch-sys :ch-http :ch-db]))
                     (proc-ops (select-keys channels [:ch-sys :ch-db :ch-http :pb-inputs :ch-ops]))
                     (go
                       (let [c (chan 1)
                             _ (>! (channels :ch-db) {:db/op :get-ratoms :ch/c-out c})
                             ratoms (<! c)]
                         (proc-render-ui (select-keys channels [:ch-db :pb-sys :ch-inputs :game-channels]) ratoms)
                         #_(game/proc-store (channels :game-channels)
                                            (ratoms :game-store))
                         #_(game/proc-worker (channels :game-channels))
                         (put! (channels :ch-sys) {:ch/topic :proc-render-ui :proc/op :render})))
                     (put! (channels :ch-sys) {:ch/topic :proc-socket :proc/op :open})
                     (put! (channels :ch-sys) {:ch/topic :proc-history :proc/op :start})
                     (put! (channels :ch-sys) {:ch/topic :proc-db :proc/op :start})

                     (put! (channels :ch-ops) {:ops/op :op/init})
                     (recur)))))
      (println "closing go block: proc-main")))

(defn ^:dev/after-load after-load []
  (put! (channels :ch-sys) {:ch/topic :proc-render-ui :proc/op :render}))

(comment

  (put! (channels :ch-sys) {:ch/topic :proc-render-containers :proc/op :mount})
  
  (put! (channels :ch-sys) {:ch/topic :proc-socket :proc/op :open})
  (put! (channels :ch-sys) {:ch/topic :proc-socket :proc/op :close})

  ;;
  )

(defonce routes ["/" {"" :page/events
                      "events" :page/events
                      "games" :page/games
                      "signin" :page/signin
                      "signup" :page/signup
                      "user" :page/user
                      "game/" {[:id ""] :page/game}
                      "stats/" {[:id ""] :page/stats-id}}])

(defn- parse-url [url]
  (merge
   {:url url}
   (bidi/match-route routes url)))

; repl only
(defonce ^:private -history nil)

(defn proc-history
  [{:keys [pb-sys ch-history ch-history-states]}]
  (let [c-sys (chan 1)]
    (sub pb-sys :proc-history c-sys)
    (go (loop [history nil]
          (alt!
            c-sys ([{:keys [proc/op]}]
                   (condp = op
                     :start (let [h (pushy/pushy
                                     (fn [pushed]
                                       #_(println "pushed" pushed)
                                       (put! ch-history-states {:history/pushed pushed})) parse-url)]
                              (pushy/start! h)
                              (set! -history h)
                              (recur history))
                     :stop (do
                             (pushy/stop! history)
                             (recur history))))
            ch-history ([{:keys [history/op history/token]}]
                        (condp = op
                          :set-token (do
                                       (pushy/set-token! history token)
                                       (recur history))))))
        (println "closing proc-history"))
    c-sys))

(defn proc-router
  [{:keys [ch-sys ch-history ch-router ml-history-states]}]
  (let [c (chan 1)
        root-el (.getElementById js/document "ui")]
    (tap ml-history-states c)
    (go (loop []
          (alt!
            c ([{:keys [history/pushed] :as v}]
               (let [{:keys [url route-params handler]} pushed
                     o {:router/handler handler
                        :history/pushed pushed}]
                 (do (put! ch-router o)
                     (recur))))))
        (println "closing proc-router"))))

(comment

  (pushy/set-token! @history (gstring/format "/u/%s" (gen/generate gen/string-alphanumeric)))

  (put! (channels :ch-history)
        {:history/op :set-token
         :history/token (gstring/format "/u/%s" (gen/generate gen/string-alphanumeric))})
  
  (put! (channels :ch-history)
        {:history/op :set-token
         :history/token "/games"})

  (a/poll! (channels :ch-history))

  (take! (channels :ch-history) (fn [v] (println v)))

  (def x (fn [c1 c2]
           (go
             (loop []
               (if-let [[v port] (alts! [c1 c2])]
                 (condp = port
                   c1 (do
                        (println (gstring/format "c1 %s" v)))
                   c2 (do
                        (println (gstring/format "c2 %s" v)))))
               (recur)))))
  
  (def c1 (chan 1))
  (def c2 (chan 1))
  (x c1 c2)
  (put! c1 1)
  (put! c2 2)


  ;;
  )

(defn make-deafult-ratoms
  []
  (let [state (r/atom {})
        local-storage (r/atom {})
        userget-user (r/cursor state [:ops/state :op/user-get :http/response :body])
        login-user (r/cursor state [:ops/state :op/login :http/response :body])
        signup-user (r/cursor state [:ops/state :op/signup :http/response :body])
        token (r/cursor local-storage ["token"])
        user (r/track! (fn []
                         (let [u1 @userget-user
                               u2 @login-user
                               u3 @signup-user
                               t @token]
                           (when t
                             (or u2 u3 u1)))))]
    {:state state
     :user user
     :local-storage local-storage
     :token token
     #_:game-store #_(game/make-store {:g/uuid (gen/generate gen/uuid)
                                       :channels (channels :game-channels)})}))

(defonce ^:private -ratoms nil)

(comment
  
  (-ratoms :state)

  ;;
  )

(defn proc-db
  [{:keys [ch-db pb-sys]}]
  (let [c-sys (chan 1)]
    (sub pb-sys :proc-db c-sys)
    (go (loop [ratoms nil
               ds nil]
          (if-let [[v port] (alts! (if ratoms [ch-db c-sys] [c-sys]))]
            (condp = port
              c-sys (let [{:keys [proc/op]} v]
                      (condp = op
                        :start (let [o (make-deafult-ratoms)
                                     ds nil]
                                 (set! -ratoms o)
                                 (put! ch-db {:db/op :sync-local-storage})
                                 (recur o ds))))
              ch-db (let [{:keys [db/op db/query ch/c-out]} v]
                      (condp = op
                        :q (let []
                             (recur ratoms ds))
                        :tx (let []
                              (recur ratoms ds))
                        :get-ratoms (let []
                                      (>! c-out ratoms)
                                      (recur ratoms ds))
                        :get-in-ratom (let [{:keys [ratoms/id ratoms/v ratoms/path]} v]
                                        (get-in @(ratoms id) path)
                                        (recur ratoms ds))
                        :assoc-in-ratom (let [{:keys [ratoms/id ratoms/v ratoms/path]} v]
                                          (swap! (ratoms id) assoc-in path v)
                                          (recur ratoms ds))
                        :merge-ratom (let [{:keys [ratoms/id ratoms/v]} v]
                                       (swap! (ratoms id) merge v)
                                       (recur ratoms ds))
                        :local-storage-get (let [{:keys [local-storage/k]} v]
                                             (>! c-out {:local-storage/v (.getItem js/localStorage k)})
                                             (recur ratoms ds))
                        :local-storage-set (let [{:keys [local-storage/k local-storage/v]} v]
                                             (.setItem js/localStorage k v)
                                             (swap! (ratoms :local-storage) assoc k v)
                                             (when c-out
                                               (>! c-out {:local-storage/v v}))
                                             (recur ratoms ds))
                        :sync-local-storage (let [k "token"
                                                  t (.getItem js/localStorage k)]
                                              (swap! (ratoms :local-storage) assoc k t)
                                              (recur ratoms ds)))))))
        (println "closing proc-db"))))

(defn proc-derived-state
  [{:keys [ml-router ml-http-res ch-db]}]
  (let [c-router (chan 1)
        c-http (chan 1)]
    (tap ml-router c-router)
    (go (loop []
          (if-let [[v port] (alts! [c-router ])]
            (condp = port
              c-router (let [o (select-keys v [:router/handler :history/pushed])]
                         #_(println o)
                         (>! ch-db {:db/op :merge-ratom
                                    :ratoms/id :state
                                    :ratoms/v o})
                         (recur)))))
        (println "closing proc-derived-state"))))

(defn proc-render-ui
  [{:keys [ch-db pb-sys] :as channels} ratoms]
  (let [c-sys (chan 1)]
    (sub pb-sys :proc-render-ui c-sys)
    (go (loop []
          (let [{:keys [proc/op]} (<! c-sys)]
            (println (gstring/format "proc-render %s" op))
            (condp = op
              :render (do
                        (render/render-ui (select-keys channels [:ch-inputs :game-channels]) ratoms)
                        (recur)))))
        (println "closing proc-render"))))

(defn proc-ops
  [{:keys [ch-db ch-http pb-inputs ch-ops] :as channels}]
  (let [c-inputs (chan 1)]
    (sub pb-inputs :inputs/ops  c-inputs)
    (go (loop []
          (if-let [[v port] (alts! [c-inputs ch-ops])]
            (cond
              (or (= port c-inputs) (= port ch-ops))
              (let [{:keys [ops/op]} v]
                (do
                  (>! ch-db {:db/op :assoc-in-ratom
                             :ratoms/id :state
                             :ratoms/path [:ops/state op :status]
                             :ratoms/v :started}))
                (condp = op
                  :op/init (go
                             (let [c-out (chan 1)
                                   _ (>! ch-ops {:ops/op :op/user-get :ch/c-out c-out})
                                   profile (<! c-out)]
                               (println profile)
                               (>! ch-db {:db/op :assoc-in-ratom
                                          :ratoms/id :state
                                          :ratoms/path [:ops/state op]
                                          :ratoms/v {:op/status :finished}})))
                  :op/login (go
                              (let [{:keys [u/username u/password]} v
                                    c-out (chan 1)
                                    req {:http/opts {:url "http://localhost:8080/login"
                                                     :method :post
                                                     :with-credentials? false
                                                     :edn-params {:u/username username
                                                                  :u/password password}}
                                         :ch/c-out c-out}
                                    _ (>! ch-http req)
                                    resp (<! c-out)]
                                (>! ch-db {:db/op :assoc-in-ratom
                                           :ratoms/id :state
                                           :ratoms/path [:ops/state op]
                                           :ratoms/v {:op/status :finished
                                                      :http/response resp}})))
                  :op/logout (go
                               (let []
                                 (>! ch-db {:db/op :local-storage-set
                                            :local-storage/k "token"
                                            :local-storage/v nil
                                            :ch/c-out (chan 1)})))
                  :op/signup (go
                               (let [{:keys [u/user]} v
                                     c-out (chan 1)
                                     req {:http/opts {:url "http://localhost:8080/user"
                                                      :method :post
                                                      :with-credentials? false
                                                      :edn-params user}
                                          :ch/c-out c-out}
                                     _ (>! ch-http req)
                                     resp (<! c-out)]
                                 (>! ch-db {:db/op :assoc-in-ratom
                                            :ratoms/id :state
                                            :ratoms/path [:ops/state op]
                                            :ratoms/v {:op/status :finished
                                                       :http/response resp}})))
                  :op/user-get (go
                                 (let [{:keys []} v
                                       c-out (chan 1)
                                       req {:http/opts {:url "http://localhost:8080/user"
                                                        :method :get
                                                        :with-credentials? false}
                                            :ch/c-out c-out}
                                       _ (>! ch-http req)
                                       resp (<! c-out)]
                                   (>! ch-db {:db/op :assoc-in-ratom
                                              :ratoms/id :state
                                              :ratoms/path [:ops/state op]
                                              :ratoms/v {:op/status :finished
                                                         :http/response resp}}))))))
            )
          (recur))
        (println "closing proc-ops"))))

(defn proc-http
  [{:keys [ch-sys ch-http ch-db]}]
  (let []
    (go
      (let [c (chan 1)
            _ (>! ch-db {:db/op :local-storage-get
                         :local-storage/k "token"
                         :ch/c-out c})
            {t :local-storage/v} (<! c)]
        (loop [token t]
          (if-let [{:keys [http/opts ch/c-out] :as v} (<! ch-http)]
            (let [resp (<! (http/request (merge opts (when token
                                                       {:with-credentials? false
                                                        :headers {"Authorization" (gstring/format "Token %s" token)}}))))]
              (>! c-out resp)
              (when (or
                     (clojure.string/includes? (:url opts) "/login")
                     (and (clojure.string/includes? (:url opts) "/user") (= (:method opts) :post)))
                (let [token (-> resp
                             (get-in [:headers "authorization"])
                             (clojure.string/split #" ")
                             (second))]
                  (>! ch-db {:db/op :local-storage-set
                             :local-storage/k "token"
                             :local-storage/v token})
                  (recur token)))
              (recur token)))))
      (println "closing proc-http"))))


(comment

  (let [c (chan 1)]
    (put! (channels :ch-http) {:http/opts {:url "https://api.github.com/users"
                                           :method :get
                                           :with-credentials? false
                                           :query-params {"since" 135}}
                               :ch/c-out c})
    (take! c (fn [o] (println o))))

  (let [c (chan 1)]
    (put! (channels :ch-http) {:http/opts {:url "http://localhost:8080/login"
                                           :method :post
                                           :with-credentials? false
                                           :edn-params {:u/username "db3zkY9rgyoI"
                                                        :u/password "ayZ8190ueI1ZJsl6j4Z82"}}
                               :ch/c-out c})
    (take! c (fn [o]

               (->
                (get-in o [:headers "authorization"])
                (clojure.string/split #" ")
                (second)
                (println)))))

  (put! (channels :ch-inputs) {:ch/topic :inputs/ops
                               :ops/op :op/login
                               :u/password "ayZ8190ueI1ZJsl6j4Z82"
                               :u/username "db3zkY9rgyoI"})

  (put! (channels :ch-inputs) {:ch/topic :inputs/ops
                               :ops/op :op/user-get
                               :u/password "ayZ8190ueI1ZJsl6j4Z82"
                               :u/username "db3zkY9rgyoI"})

  ;;
  )

(defn proc-socket
  [{:keys [pb-sys ch-socket-in ch-socket-out]}]
  (let [c-sys (chan 1)
        w (transit/writer :json)
        r (transit/reader :json)]
    (sub pb-sys :proc-socket c-sys)
    (go (loop [ws nil]
          (when-let [[v port] (alts! [c-sys ch-socket-out])]
            (condp = port
              c-sys (let [{:keys [proc/op]} v]
                      (println (gstring/format "proc-socket %s" op))
                      (condp = op
                        :open (let [ws (WebSocket. #js {:autoReconnect false})]
                                (.open ws "ws://localhost:8080/ws")
                                (.listen ws WebSocket.EventType.MESSAGE
                                         (fn [^:goog.net.WebSocket.MessageEvent ev]
                                           (let [blob (.-message ev)]
                                             (-> blob
                                                 (.text)
                                                 (.then (fn [s]
                                                          (let [o (transit/read r s)]
                                                            (put! ch-socket-in o))))))))
                                (recur ws))
                        :close (do
                                 (.close ws)
                                 (recur nil))))
              ch-socket-out (let [{:keys [ws/data]} v]
                              (let [s (transit/write w data)
                                    blob (js/Blob. [s] #js {:type "application/transit+json"})]
                                (-> blob
                                    (.arrayBuffer)
                                    (.then (fn [ab]
                                             (.send ws ab)))))
                              (recur ws)))))
        (println "proc-socket closing"))
    c-sys))

(comment

  (put! (channels :ch-socket-out) {:ws/data {:some "data"}})

  ;;
  )

