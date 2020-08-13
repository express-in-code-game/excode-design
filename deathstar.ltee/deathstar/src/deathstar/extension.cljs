(ns deathstar.extension
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [goog.string :refer [format]]
   [clojure.string :as string]
   [cljs.reader :refer [read-string]]
   [clojure.pprint :refer [pprint]]

   [cljctools.vscode :as vscode]
   #_[pad.cljsjs1]
   #_[pad.selfhost1]))

(def channels (let [main| (chan 10)
                    main|m (mult main|)
                    log| (chan 100)
                    log|m (mult log|)
                    cmd| (chan 100)
                    cmd|m (mult cmd|)
                    ops| (chan 10)
                    ops|m (mult ops|)
                    conn-status| (chan (sliding-buffer 10))
                    conn-status|m (mult conn-status|)
                    conn-status|x (mix conn-status|)
                    editor| (chan 10)
                    editor|m (mult editor|)
                    #_editor|p #_(pub (tap editor|m (chan 10)) channels/TOPIC (fn [_] 10))]
                {:main| main|
                 :main|m main|m
                 :log| log|
                 :log|m log|m
                 :cmd| cmd|
                 :cmd|m cmd|m
                 :ops| ops|
                 :conn-status| conn-status|
                 :conn-status|m conn-status|m
                 :conn-status|x conn-status|x
                 :ops|m ops|m
                 :editor| editor|
                 :editor|m editor|m}))

(declare proc-main proc-ops proc-log)


(defn main [])

; repl only
(def proc-main-state (atom {}))

(defn default-commands
  []
  ["deathstar.open"
   "deathstar.ping"])

(defn proc-main
  [channels ctx]
  #_(do
      (prn (pad.cljsjs1/test1)))
  (let [pid [:proc-main]
        {:keys [main| main|m log| ops|]} channels
        main|t (tap main|m (chan 10))
        log (fn [& args] (println args))]
    (go (loop [state {:channels channels
                      :ctx ctx
                      :procs {}
                      :editor nil
                      :activated? false}]
          (reset! proc-main-state state)
          (try
            (if-let [v (<! main|t)]
              (condp = (:op v)
                :main/init (let [{:keys [channels ctx]} state]
                             (js/console.log "deathstar activating")
                             (js/console.log vscode/vscode.workspace.rootPath)
                             (vscode/show-information-message vscode/vscode "deathstar actiavting")))
              (recur state))
            (catch js/Error e (do (println "; proc-main error, will resume")
                                  (js/console.log e)
                                  #_(clojure.stacktrace/print-stack-trace e)
                                  )))
          (recur state))
        (log "; proc-main go-block exiting, but it shouldn't"))))



; repl only
(def ^:private proc-ops-state (atom {}))


(reduce (fn [ag x] (assoc ag x x)) {} #{1 2 3})

; repl only
(def ^:private proc-log-state (atom {}))


(defonce _ (let []
             #_(put! (channels :main|) {:op :main/init})
             (proc-main channels {})))

(def activate (let [ ]
                (fn [context]
                  (put! (channels :main|) {:op :main/init}))))
(def deactivate (let []
                  (fn []
                    (put! (channels :main|) {:op :main/init}))))

(def exports #js {:activate activate
                  :deactivate deactivate})

(when (exists? js/module)
  (set! js/module.exports exports))




#_(defn reload
    []
    (.log js/console "Reloading...")
    (js-delete js/require.cache (js/require.resolve "./main")))
