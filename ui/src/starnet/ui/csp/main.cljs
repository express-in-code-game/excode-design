(ns starnet.ui.csp.main
  (:require
   [clojure.repl :refer [doc]]
   [reagent.core :as r]
   [clojure.core.async :as a :refer [<! >!  timeout chan alt! go
                                     alts!  take! put!
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
   

   [starnet.common.alpha.spec]

   [starnet.ui.alpha.spec]
   [starnet.ui.alpha.repl]
   [starnet.ui.alpha.tests])
  (:import [goog.net XhrIo EventType WebSocket]
           [goog Uri]
           goog.history.Html5History))

(declare proc-main proc-socket proc-render-containers
        proc-history )

(enable-console-print!)

(def channels (let [ch-proc-main (chan 1)
                    ch-sys (chan (sliding-buffer 10))
                    pb-sys (pub ch-sys :ch/topic (fn [_] (sliding-buffer 10)))
                    ch-socket (chan (sliding-buffer 10))
                    ch-history (chan (sliding-buffer 10))
                    ch-history-states (chan (sliding-buffer 10))
                    pb-history-states (pub ch-history-states :ch/topic (fn [_] (sliding-buffer 10)))]
                {:ch-proc-main ch-proc-main
                 :ch-sys ch-sys
                 :pb-sys pb-sys
                 :ch-history ch-history
                 :ch-history-states ch-history-states
                 :pb-history-states pb-history-states
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
                     (proc-render-containers (select-keys channels [:pb-sys :ch-sys]))
                     (proc-socket (select-keys channels [:pb-sys :ch-sys :ch-socket]))
                     (proc-history (select-keys channels [:pb-sys :ch-sys :ch-history :ch-history-states]))
                     
                     (put! (channels :ch-sys) {:ch/topic :proc-render-containers :proc/op :mount})
                     (put! (channels :ch-sys) {:ch/topic :proc-socket :proc/op :open})
                     (put! (channels :ch-sys) {:ch/topic :proc-history :proc/op :start})
                     )))
        (recur))
      (println "closing go block: proc-main")))

(defn proc-render-containers
  [{:keys [pb-sys]}]
  (let [c (chan 1)
        root-el (.getElementById js/document "ui")]
    (sub pb-sys :proc-render-containers c)
    (go (loop []
          (when-let [{:keys [proc/op]} (<! c)]
            (println (gstring/format "proc-render-containers %s" op))
            (condp = op
              :mount (do (r/render [:<>
                                    [:div {:id "div-1"}]
                                    [:div {:id "div-2"}]
                                    [:a {:href "/a"} "a"]
                                    [:br]
                                    [:a {:href "/b"} "b"]
                                    [:div {:id "div-3"}]] root-el))
              :unmount (r/render nil root-el)))
          (recur))
        (println "proc-render-containers closing"))
    c))

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
                       (recur nil))))
          (recur ws))
        (println "proc-render-containers closing"))
    c)
  )

(comment

  (put! (channels :ch-sys) {:ch/topic :proc-render-containers :proc/op :mount})
  
  (put! (channels :ch-sys) {:ch/topic :proc-socket :proc/op :open})
  (put! (channels :ch-sys) {:ch/topic :proc-socket :proc/op :close})

  ;;
  )

(def routes ["/" {"" :page/events
                  "games/" :page/games
                  "events/" :page/events
                  "u/" {[:id ""] :page/user}}])

(defn- parse-url [url]
  (merge
   {:url url}
   (bidi/match-route routes url)))

; repl only
(def ^:private history (atom nil))
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
                                    (fn [matched]
                                      (println matched)
                                      (put! ch-history-states matched)) parse-url)]
                             (pushy/start! h)
                             (reset! history h)
                             (recur h))
                    :stop (do
                            (pushy/stop! h))))
              ch-history (let [{:keys [history/op history/token]} v]
                           (condp = op
                             :set-token   (pushy/set-token! h token)))))
          (recur h)))
    c))



(comment

  (pushy/set-token! @history (gstring/format "/u/%s" (gen/generate gen/string-alphanumeric)))

  (put! (channels :ch-history)
        {:history/op :set-token
         :history/token (gstring/format "/u/%s" (gen/generate gen/string-alphanumeric))})

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


; pages are templates
; on navigation, proc-pages conveys to page channel
; page renders template, then conveys to procs and they render elements

(defn proc-page-events
  [{:keys [pb-sys]}]
  (let [c (chan 1)
        root-el (.getElementById js/document "ui")]
    (sub pb-sys :proc-render-containers c)
    (go (loop []
          (when-let [{:keys [proc/op]} (<! c)]
            (println (gstring/format "proc-render-containers %s" op))
            (condp = op
              :mount (do (r/render [:<>
                                    [:div {:id "div-1"}]
                                    [:div {:id "div-2"}]
                                    [:div {:id "div-3"}]] root-el))
              :unmount (r/render nil root-el)))
          (recur))
        (println "proc-render-containers closing"))
    c))

(defn proc-page-profile
  [])

(defn proc-page-user-games
  [])

(defn proc-page-games
  [])

(defn proc-tab-game
  [])

(defn proc-page-event
  [])

(defn proc-page-event-bracket
  [])

