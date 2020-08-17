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

   #_[cljctools.vscode.spec :as host.sp]
   [cljctools.vscode.protocols :as host.p]
   [cljctools.vscode.api :as host.api]
   [deathstar.spec :as sp]
   [cljctools.cljs-cache]
   [cljctools.cljs-self-hosting]
   #_[pad.cljsjs1]
   #_[pad.selfhost1]))

#_(def channels (let [main| (chan 10)
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

(defn create-channels
  []
  (let [extension-ops| (chan 10)
        extension-ops|m (mult extension-ops|)
        extension-ops|x (mix extension-ops|)]
    {:extension-ops| extension-ops|
     :extension-ops|m extension-ops|m
     :extension-ops|x extension-ops|x}))

(defn default-commands
  []
  ["deathstar.open"
   "deathstar.ping"])

(defn create-proc-ops
  [channels ctx]
  (let [{:keys [extension-ops|m]} channels
        extension-ops|t (tap extension-ops|m (chan 10))]
    (go
      (loop []
        (if-let [v (<! extension-ops|t)]
          (condp = (:op v)
            (sp/op :extension-ops|
                   :host/extension-activate) (let []
                                               (js/console.log "deathstar activating")
                                               (js/console.log host.api/vscode.workspace.rootPath)
                                               (host.api/show-information-message host.api/vscode "deathstar actiavting")
                                               (<! (cljctools.cljs-cache/init "/home/user/code/deathstar/build/resources/out/deathstar-bootstrap"))
                                               (prn (cljctools.cljs-self-hosting/test1)))))
        (recur))
      (println "; proc-ops go-block exiting"))))


(defn create-proc-log
  [channels ctx]
  (let []
    (go (loop []
          (<! (chan 1))
          (recur))
        (println "; proc-log go-block exiting"))))