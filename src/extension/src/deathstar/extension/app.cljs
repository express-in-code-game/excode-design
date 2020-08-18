(ns deathstar.extension.app
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [goog.string :refer [format]]
   [clojure.string :as string]
   [cljs.reader :refer [read-string]]
   [clojure.pprint :refer [pprint]]

   [cljctools.vscode.spec :as host.sp]
   [cljctools.vscode.api :as host.api]
   [deathstar.extension.spec :as sp]
   [cljctools.self-hosted.compiler :as compiler.api]
   [cljctools.self-hosted.api :as self-hosted.api]
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
  (let [ext-ops| (chan 10)
        ext-ops|m (mult ext-ops|)
        ext-ops|x (mix ext-ops|)
        ext-cmd| (chan 10)
        ext-cmd|m (mult ext-cmd|)
        tab-state| (chan (sliding-buffer 10))
        tab-state|m (mult tab-state|)]
    {:ext-ops| ext-ops|
     :ext-ops|m ext-ops|m
     :ext-ops|x ext-ops|x
     :ext-cmd| ext-cmd|
     :ext-cmd|m ext-cmd|m
     :tab-state| tab-state|
     :tab-state|m tab-state|m}))


(def ^:dynamic *extension-compiler* (compiler.api/create-compiler))

(def ^:dynamic *solution-compiler* (compiler.api/create-compiler))

(def tmp1 (atom 0))

(defn bar [])

(comment

  (def a 'hello)
  (-> #'a meta)

  (binding [*ns* 'foo]
    (pr-str
     `(do
        (type type)
        (bar)
        ~a)))

  (take! (compiler.api/eval-str
          *solution-compiler*
          {:code
           "
            (cljs.core/type cljs.core/type)
            #_(cljs.core/type deathstar.tabapp.solution-space.main/state)
    "
           :nspace 'deathstar.tabapp.solution-space.main})
         (fn [data]
           (prn data)
           (prn (type (:value data)))))

  (take! (compiler.api/eval-str
          *extension-compiler*
          {:code
           "
  (cljs.core/type deathstar.main/main)
            
    "
           :nspace 'deathstar.main})
         (fn [data]
           (prn data)
           (prn (type (:value data)))))
  
  (take! (compiler.api/eval-str
          *extension-compiler*
          {:code
           "
       #_(cljs.core/type deathstar.extension/tmp1)
            (type deathstar.extension/tmp1)
             (swap! deathstar.extension/tmp1 inc)
            #_@deathstar.extension/tmp1
            @tmp1
            
            
    "
           :nspace 'deathstar.extension})
         (fn [data]
           (prn data)
           (prn (type (:value data)))))


  ;;
  )

(defn create-proc-ops
  [channels ctx]
  (let [{:keys [ext-ops| ext-ops|m host-evt|m ext-cmd| ext-cmd|m
                tab-state|]} channels
        {:keys [host state]} ctx
        ext-ops|t (tap ext-ops|m (chan 10))
        ext-cmd|t (tap ext-cmd|m (chan 10))
        relevant-host-evt? (fn [v]  ((host.sp/ops #{:host/extension-activate :host/extension-deactivate}) (:op v)))
        host-evt|t (tap host-evt|m (chan 10 (comp (filter (every-pred relevant-host-evt?)))))]
    (go
      (loop []
        (when-let [[v port] (alts! [ext-ops|t host-evt|t ext-cmd|t])]
          (condp = port
            host-evt|t (condp = (:op v)
                         
                         (host.sp/op
                          :host-evt|
                          :host/extension-activate)
                         (let []
                           (js/console.log "deathstar activating")
                           (host.api/show-info-msg host "deathstar actiavting")
                           (host.api/register-commands host {:ids sp/cmd-keys
                                                             :cmd| ext-cmd|})
                           (<! (compiler.api/init
                                *extension-compiler*
                                {:path "/home/user/code/deathstar/build/extension/resources/out/deathstar-bootstrap"
                                 :load-on-init '#{deathstar.main clojure.core.async}}))
                           #_(<! (compiler.api/init
                                  *solution-compiler*
                                  {:path "/home/user/code/deathstar/build/resources/out/deathstar-bootstrap-solution-space"
                                   :load-on-init '#{deathstar.tabapp.solution-space.main
                                                    clojure.core.async}}))
                           #_(prn (self-hosted.api/test1))))
            
            ext-ops|t (condp = (:op v)
                        :some-msg-from-tab (do nil))
            ext-cmd|t (condp = (:op v)
                        (sp/cmd "deathstar.open")
                        (host.api/show-info-msg host "deathstar.open")

                        (sp/cmd "deathstar.ping")
                        (host.api/show-info-msg host "deathstar.ping")

                        (sp/cmd "deathstar.open-solution-space-tab")
                        (let [tab (host.api/create-tab
                                   host
                                   {:id "solution-space-tab"
                                    :title "solution space"
                                    :script-path "resources/out/deathstar-tabapp-solution-space/main.js"
                                    :html-path "resources/solution-space.html"
                                    :script-replace "/out/tabapp.js"
                                    :tab-msg| ext-ops|
                                    :tab-state| tab-state|})]
                          (swap! state assoc :solution-space-tab tab))

                        (sp/cmd "deathstar.solution-tab-eval")
                        (let [tab (get @state :solution-space-tab)]
                          (host.api/send-tab
                           tab
                           (sp/vl :solution-tab| {:op :solution-space/eval :data "hello"}))))))
        (recur))
      (println "; proc-ops go-block exiting"))))


(defn create-proc-log
  [channels ctx]
  (let []
    (go (loop []
          (<! (chan 1))
          (recur))
        (println "; proc-log go-block exiting"))))