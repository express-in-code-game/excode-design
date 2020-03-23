(ns starnet.ui.csp.main
  (:require
   [clojure.repl :refer [doc]]
   [reagent.core :as r]
   [clojure.core.async :as a :refer [<! >!  timeout chan alt! go
                                     alts!  take! put! mult tap untap
                                     pub sub sliding-buffer mix admix unmix]]
   [goog.string :as gstring]
   [goog.string.format]

   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check :as tc]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]


   [bidi.bidi :as bidi]
   [pushy.core :as pushy]

   [starnet.ui.csp.render :as render]
   
   [datascript.core :as ds]

   [starnet.common.alpha.spec]

   [starnet.ui.alpha.spec]
   [starnet.ui.alpha.repl]
   [starnet.ui.alpha.tests])
  (:import [goog.net XhrIo EventType WebSocket]
           [goog Uri]
           goog.history.Html5History))

(declare proc-main proc-socket proc-render-containers proc-http proc-db
        proc-history proc-router proc-derived-state proc-render-ui)

(enable-console-print!)

(defonce channels (let [ch-proc-main (chan 1)
                        ch-sys (chan (sliding-buffer 10))
                        pb-sys (pub ch-sys :ch/topic (fn [_] (sliding-buffer 10)))
                        ch-socket (chan (sliding-buffer 10))
                        ch-history (chan (sliding-buffer 10))
                        ch-router (chan (sliding-buffer 10))
                        ml-router (mult ch-router)
                        ch-history-states (chan (sliding-buffer 10))
                        ml-history-states (mult ch-history-states)
                        ch-db (chan (sliding-buffer 10))
                        ch-http (chan (sliding-buffer 10))
                        ch-http-res (chan (sliding-buffer 10))
                        ml-http-res (mult ch-http-res)]
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
                     :ch-http-res ch-http-res
                     :ml-http-res ml-http-res
                     :ch-socket ch-socket}))

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
                     (proc-socket (select-keys channels [:pb-sys :ch-sys :ch-socket]))
                     (proc-http (select-keys channels [:pb-sys :ch-sys :ch-http :ch-http-res]))
                     (proc-history (select-keys channels [:pb-sys :ch-sys :ch-history :ch-history-states]))
                     (proc-router (select-keys channels [:ch-sys :ch-history :ml-history-states :ch-router]))
                     (proc-db (select-keys channels [:pb-sys :ch-db]))
                     (proc-derived-state (select-keys channels [:ml-router :ml-http-res :ch-db]))
                     (proc-render-ui (select-keys channels [:ch-db]))


                     (put! (channels :ch-sys) {:ch/topic :proc-socket :proc/op :open})
                     (put! (channels :ch-sys) {:ch/topic :proc-history :proc/op :start})
                     (put! (channels :ch-sys) {:ch/topic :proc-db :proc/op :start})
                     (recur)))))
      (println "closing go block: proc-main")))


(defn proc-socket
  [{:keys [pb-sys ch-socket]}]
  (let [c (chan 1)]
    (sub pb-sys :proc-socket c)
    (go (loop [ws nil]
          (when-let [{:keys [proc/op]} (<! c)]
            (println (gstring/format "proc-socket %s" op))
            (condp = op
              :open (let [ws (WebSocket. #js {:autoReconnect false})]
                      (.open ws "ws://localhost:8080/ws")
                      (.listen ws WebSocket.EventType.MESSAGE (fn [^:goog.net.WebSocket.MessageEvent ev]
                                                                (println (.-message ev))))
                      (recur ws))
              :close (do
                       (.close ws)
                       (recur nil)))))
        (println "proc-render-containers closing"))
    c))

(defn proc-http
  [{:keys [ch-sys ch-http ch-http-res]}]
  (let []
    (go (loop []
          (if-let [{:keys [http/url http/data] :as v} (<! ch-http)]
            (let [resp (<! '(http-req))]
              (do (put! ch-http-res resp)
                  (recur)))))
        (println "closing proc-http"))))

(comment

  (put! (channels :ch-sys) {:ch/topic :proc-render-containers :proc/op :mount})
  
  (put! (channels :ch-sys) {:ch/topic :proc-socket :proc/op :open})
  (put! (channels :ch-sys) {:ch/topic :proc-socket :proc/op :close})

  ;;
  )

(defonce routes ["/" {"" :page/events
                      "games" :page/games
                      "events" :page/events
                      "settings" :page/settings
                      "login" :page/login
                      "game/" {[:id ""] :page/game}
                      "u/" {"games" :page/user-games
                            [:id ""] :page/userid
                            [:id "games"] :page/userid-games}}])

(defn- parse-url [url]
  (merge
   {:url url}
   (bidi/match-route routes url)))

; repl only
(defonce ^:private history (atom nil))
(defn proc-history
  [{:keys [pb-sys ch-history ch-history-states]}]
  (let [c (chan 1)]
    (sub pb-sys :proc-history c)
    (go (loop [h nil]
          (if-let [[v port] (alts! [c ch-history])]
            (condp = port
              c (let [{:keys [proc/op]} v]
                  (condp = op
                    :start (let [h (pushy/pushy
                                    (fn [pushed]
                                      #_(println "pushed" pushed)
                                      (put! ch-history-states {:history/pushed pushed})) parse-url)]
                             (pushy/start! h)
                             (reset! history h)
                             (recur h))
                    :stop (do
                            (pushy/stop! h)
                            (recur h))))
              ch-history (let [{:keys [history/op history/token]} v]
                           (condp = op
                             :set-token (do
                                          (pushy/set-token! h token)
                                          (recur h)))))))

        (println "closing proc-history"))
    c))

; for dev realod only
(defonce ^:private route (atom nil))
(defn ^:dev/after-load after-load []
  (put! (channels :ch-router) @route))

(defn proc-router
  [{:keys [ch-sys ch-history ch-router ml-history-states]}]
  (let [c (chan 1)
        root-el (.getElementById js/document "ui")]
    (tap ml-history-states c)
    (go (loop []
          (if-let [{:keys [history/pushed] :as v} (<! c)]
            (let [{:keys [url route-params handler]} pushed
                  o {:router/handler handler
                     :history/pushed pushed}]
              (do (put! ch-router o)
                  (reset! route o)
                  (recur)))))
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
  (let []
    {:state (r/atom {})}))

(def ^:private rtoms nil)

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
                        :start (let [ratoms (make-deafult-ratoms)
                                     ds nil]
                                 (set! rtoms ratoms)
                                 (recur ratoms ds))))
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
                                       (recur ratoms ds)))))))
        (println "closing proc-db"))))

(defn proc-derived-state
  [{:keys [ml-router ml-http-res ch-db]}]
  (let [c-router (chan 1)
        c-http (chan 1)]
    (tap ml-router c-router)
    (tap ml-http-res  c-http)
    (go (loop []
          (if-let [[v port] (alts! [c-router c-http])]
            (condp = port
              c-router (let [o (select-keys v [:router/handler :history/pushed])]
                         #_(println o)
                         (>! ch-db {:db/op :merge-ratom
                                    :ratoms/id :state
                                    :ratoms/v o})
                         (recur))
              c-http (let []
                       (recur)))))
        (println "closing proc-derived-state"))))

(defn proc-render-ui
  [{:keys [ch-db] :as channels}]
  (let []
    (go (loop [ratoms nil]
          #_(println "ratoms" ratoms)
          (when-not ratoms
            (let [c (chan 1)]
              (>! ch-db {:db/op :get-ratoms :ch/c-out c})
              (recur (<! c))))
          (let []
            (render/render-ui channels ratoms)))
        (println "closing proc-render"))))