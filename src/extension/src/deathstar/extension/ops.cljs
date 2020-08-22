(ns deathstar.extension.ops
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [goog.string :refer [format]]
   [clojure.string :as string]
   [cljs.reader :refer [read-string]]
   [clojure.pprint :refer [pprint]]

   [cljctools.self-hosted.compiler :as compiler.api]
   [cljctools.self-hosted.api :as self-hosted.api]

   [cljctools.vscode.spec :as host.spec]
   [cljctools.vscode.api :as host.api]

   [cljctools.net.core.spec :as net.spec]
   [deathstar.extension.spec :as spec]
   [deathstar.core.spec :as core.spec]
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
        extension-ops|x (mix extension-ops|)
        cmd| (chan 10)
        cmd|m (mult cmd|)
        tab-state| (chan (sliding-buffer 10))
        tab-state|m (mult tab-state|)]
    {::spec/cmd| cmd|
     ::spec/cmd|m cmd|m
     ::spec/tab-state| tab-state|
     ::spec/tab-state|m tab-state|m

     ::core.spec/extension-ops| extension-ops|
     ::core.spec/extension-ops|m extension-ops|m}))


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
  (let [{:keys [::core.spec/extension-ops| ::core.spec/extension-ops|m
                ::host.spec/evt|m :http|
                ::spec/cmd| ::spec/cmd|m ::spec/tab-state|]
         socket|m ::net.spec/recv|m} channels
        {:keys [host state]} ctx
        extension-ops|t (tap extension-ops|m (chan 10))
        cmd|t (tap cmd|m (chan 10))
        socket|t (tap socket|m (chan 10))
        relevant-evt? (fn [v]  ((host.spec/ops #{::host.spec/extension-activate ::host.spec/extension-deactivate}) (:op v)))
        evt|t (tap evt|m (chan 10 (comp (filter (every-pred relevant-evt?)))))]
    (go
      (loop []
        (when-let [[v port] (alts! [extension-ops|t evt|t cmd|t])]
          (condp = port
            evt|t (condp = (:op v)

                    (host.spec/op
                     ::host.spec/evt|
                     ::host.spec/extension-activate)
                    (let []
                      (println ::host.spec/extension-activate)
                      (host.api/show-info-msg host "Death Star activating")
                      (host.api/register-commands host {:ids spec/cmd-ids
                                                        ::host.spec/cmd| cmd|})
                      #_(<! (compiler.api/init
                             *extension-compiler*
                             {:path "/home/user/code/deathstar/build/extension/resources/out/deathstar-bootstrap"
                              :load-on-init '#{deathstar.main clojure.core.async}}))
                      #_(<! (compiler.api/init
                             *solution-compiler*
                             {:path "/home/user/code/deathstar/build/resources/out/deathstar-bootstrap-solution-space"
                              :load-on-init '#{deathstar.tabapp.solution-space.main
                                               clojure.core.async}}))
                      #_(prn (self-hosted.api/test1))))

            extension-ops|t (condp = (:op v)
                              ::spec/some-op (do nil))
            socket|t (let []
                       (println "data from socket" v))
            cmd|t (condp = (::host.spec/cmd-id v)

                    (spec/cmd-id "deathstar.open")
                    (host.api/show-info-msg host "deathstar.open")

                    (spec/cmd-id "deathstar.ping")
                    (host.api/show-info-msg host "deathstar.ping")

                    (spec/cmd-id "deathstar.gui.open")
                    (let [tab (host.api/create-tab
                               host
                               {:id "gui-tab"
                                :title "Death Star"
                                :script-path "resources/out/deathstar-gui/main.js"
                                :html-path "resources/gui.html"
                                :script-replace "./out/deathstar-gui/main.js"
                                :msg| extension-ops|
                                :state| tab-state|})]
                      (swap! state assoc ::spec/gui-tab tab))

                    (spec/cmd-id "deathstar.solution-tab-eval")
                    (let [tab (get @state ::spec/gui-tab)]
                      (host.api/send-tab
                       tab
                       (core.spec/vl ::core.spec/gui-ops| {:op ::core.spec/update-gui-state :data "hello"}))))))
        (recur))
      (println "; proc-ops go-block exiting"))))


(defn create-proc-log
  [channels ctx]
  (let []
    (go (loop []
          (<! (chan 1))
          (recur))
        (println "; proc-log go-block exiting"))))