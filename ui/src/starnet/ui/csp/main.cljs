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
   [starnet.common.pad.reagent1]
   

   [starnet.common.alpha.spec]

   [starnet.ui.alpha.spec]
   [starnet.ui.alpha.repl]
   [starnet.ui.alpha.tests])
  (:import [goog.net XhrIo EventType WebSocket]
           [goog Uri]
           goog.history.Html5History))

(declare proc-main proc-socket proc-render-containers proc-http
        proc-history proc-router proc-derived-state-ui proc-renderer)

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
                        ;; ch-derived-state-ui (chan (sliding-buffer 10))
                        ;; ml-derived-state-ui (mult ch-derived-state-ui)
                        ch-http (chan (sliding-buffer 10))
                        ch-http-res (chan (sliding-buffer 10))
                        ml-http-res (mult ch-http-res)]
                    {:ch-proc-main ch-proc-main
                     :ch-sys ch-sys
                     :pb-sys pb-sys
                     :ch-history ch-history
                     :ch-router ch-router
                     :ml-router ml-router
                     :ch-history-states ch-history-states
                     :ml-history-states ml-history-states
                    ;;  :ch-derived-state-ui ch-derived-state-ui
                    ;;  :ml-derived-state-ui ml-derived-state-ui
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
                     #_(proc-derived-state-ui (select-keys channels [:ch-derived-state-ui :ml-router :ml-http-res]))
                     #_(proc-renderer channels)
                     (render/proc-page-user-games channels)
                     (render/proc-page-userid channels)
                     (render/proc-page-games channels)
                     (render/proc-page-not-found channels)
                     (render/proc-page-userid-games channels)
                     (render/proc-page-events channels)
                     (render/proc-page-settings channels)
                     (render/proc-page-login channels)
                     (render/proc-page-game channels)
                     

                     (put! (channels :ch-sys) {:ch/topic :proc-socket :proc/op :open})
                     (put! (channels :ch-sys) {:ch/topic :proc-history :proc/op :start})
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
                                      (println "pushed" pushed)
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



