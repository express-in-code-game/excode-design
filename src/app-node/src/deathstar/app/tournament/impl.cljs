(ns deathstar.app.tournament.impl
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.core.async.impl.protocols :refer [closed?]]
   [clojure.string :as str]
   [cljs.core.async.interop :refer-macros [<p!]]
   [goog.string.format]
   [goog.string :refer [format]]
   [goog.object]
   [cljs.reader :refer [read-string]]

   [cljctools.csp.op.spec :as op.spec]
   [cljctools.cljc.core :as cljc.core]

   [deathstar.app.spec :as app.spec]
   [deathstar.app.chan :as app.chan]

   [deathstar.app.tournament.spec :as app.tournament.spec]
   [deathstar.app.tournament.chan :as app.tournament.chan]))

(defonce fs (js/require "fs"))
(defonce path (js/require "path"))


(defn create-proc-ops
  [channels ctx opts]
  (let [{:keys [::app.tournament.chan/ops|
                ::app.tournament.chan/release|]} channels

        {:keys [::app.spec/state*
                ::app.spec/ipfs*
                ::app.spec/orbitdb*]} ctx

        {:keys [::app.tournament.spec/frequency
                ::app.spec/peer-name
                ::app.spec/peer-id
                ::app.spec/host-id]} opts

        tournament*  (atom {})
        watch-key ::watch
        _ (add-watch tournament* watch-key
                     (fn [k atom-ref oldstate newstate]
                       (swap! state* update-in [::tournaments] assoc frequency newstate)))
        _ (reset! tournament* (merge
                               (select-keys opts [::app.tournament.spec/frequency])
                               {::app.spec/peer-metas {}
                                ::app.spec/host-id nil}))
        ipfs @ipfs*
        eventlog (<p! (.eventlog @orbitdb*
                                 frequency
                                 (clj->js {"accessController"
                                           {"write" ["*"] #_[(.. orbitdb -identity -publicKey)]}})))
        close-emit-meta| (chan 1)
        close-iterate-metas| (chan 1)
        release
        (fn []
          (go
            (remove-watch tournament* watch-key)
            (close! close-emit-meta|)
            (close! close-iterate-metas|)
            (<p! (.unsubscribe (.-pubsub ipfs) frequency))
            #_(<p! (.drop eventlog))
            (<p! (.close eventlog))))]
    (go
      (.on (.-events eventlog)
           "replicate.progress"
           (fn [address hash entry progress have]
             (let [value (read-string (.-value (.-payload entry)))]
               (put! ops| value))))
      #_(.on (.-events eventlog)
             "replicated"
             (fn [address]
               (-> eventlog
                   (.iterator  #js {"gt" (::eventlog-prev-hash @tournanment*)})
                   (.collect)
                   (.map (fn [entry]
                           (let [value (read-string (.-value (.-payload entry)))]
                             (put! ops| value))
                           (when (empty? (.-next entry))
                             (swap! tournanment*
                                    assoc
                                    ::eventlog-prev-hash
                                    (.-hash entry))))))))
      #_(.on (.-events eventlog)
             "write"
             (fn [address entry heads]
               (swap! tournanment*
                      assoc
                      ::eventlog-prev-hash
                      (.-hash entry))))
      (<p! (.load eventlog))
      (let [events (-> eventlog
                       (.iterator  #js {"limit" -1
                                        "reverse" false})
                       (.collect)
                       (vec))]
        (println ::count-events (count events))
        (doseq [event events]
          (let [value (read-string (.-value (.-payload event)))]
            (put! ops| (merge value
                              {::replay? true})))
          #_(when (empty? (.-next event))
              (swap! tournanment*
                     assoc
                     ::eventlog-prev-hash
                     (.-hash event)))))
      (let [{:keys [::app.spec/frequency
                    ::app.spec/peer-name
                    ::app.spec/host-id
                    ::app.spec/peer-id]} opts
            own-peer-id (get @state* ::app.spec/peer-id)
            text-encoder (js/TextEncoder.)
            text-decoder (js/TextDecoder.)]
        (<p! (.subscribe (.-pubsub ipfs)
                         frequency
                         (fn [msg]
                           (when-not (= own-peer-id (.-from msg))
                             (let [peer-id (.-from msg)]
                               (swap! tournament* update-in
                                      [::app.spec/peer-metas peer-id]
                                      merge (merge
                                             (read-string (.decode text-decoder  (.-data msg)))
                                             {::app.spec/peer-id peer-id
                                              ::app.spec/received-at (.now js/Date)})))
                             #_(do
                                 (println (format "id: %s" id))
                                 (println (format "from: %s" (.-from msg)))
                                 (println (format "data: %s" (.decode text-decoder  (.-data msg))))
                                 (println (format "topicIDs: %s" msg.topicIDs)))))))
        (go (loop [counter 0]
              (<p! (.publish (.-pubsub ipfs)
                             frequency
                             (-> text-encoder
                                 (.encode  (pr-str {::app.spec/peer-id peer-id
                                                    ::app.spec/counter counter})))))
              (alt!
                [(timeout 2000)]
                ([value c|]
                 (recur (inc counter)))
                [close-emit-meta|]
                ([_ _]
                 (do nil)))))
        (go (loop []
              (alt!
                [(timeout 4000)]
                ([value c|]
                 (let [own-peer-id (get @state* ::app.spec/peer-id)]
                   (doseq [[peer-id {:keys [::app.spec/received-at]
                                     :as peer-meta}] (get-in @tournament* [::app.spec/peer-metas])
                           :when (not= peer-id own-peer-id)
                           :when (> (- (.now js/Date) received-at) 8000)]
                     (println ::removing-peer-from-tournament)
                     (swap! tournament* update-in
                            [::app.spec/peer-metas]
                            dissoc peer-id)))
                 (recur))
                [close-iterate-metas|]
                ([_ _]
                 (do nil))))))
      (loop []
        (when-let [[value port] (alts! [ops| release|])]
          (condp = port

            release|
            (let [{:keys [::op.spec/out|]} value]
              (<! (release))
              (close! out|))

            ops|
            (do
              (condp = (select-keys value [::op.spec/op-key ::op.spec/op-type ::op.spec/op-orient])

                {::op.spec/op-key ::app.tournament.chan/create-tournament
                 ::op.spec/op-type ::op.spec/request-response
                 ::op.spec/op-orient ::op.spec/request}
                (let [{:keys [::app.spec/frequency
                              ::app.spec/peer-name
                              ::app.spec/peer-id
                              ::app.spec/host-id
                              ::replay?
                              ::op.spec/out|]} value]
                  (when-not replay?
                    (<p! (.add eventlog
                               (pr-str (merge
                                        {::op.spec/op-key ::app.tournament.chan/create-tournament
                                         ::op.spec/op-type ::op.spec/request-response
                                         ::op.spec/op-orient ::op.spec/request}
                                        {::app.spec/frequency frequency
                                         ::app.spec/peer-name peer-name
                                         ::app.spec/peer-id peer-id
                                         ::app.spec/host-id host-id})))))
                  (swap! tournament* assoc ::app.spec/host-id host-id)
                  (close! out|))

                {::op.spec/op-key ::app.tournament.chan/join-tournament
                 ::op.spec/op-type ::op.spec/request-response
                 ::op.spec/op-orient ::op.spec/request}
                (let [{:keys [::app.spec/frequency
                              ::app.spec/peer-name
                              ::app.spec/peer-id
                              ::replay?
                              ::op.spec/out|]} value]
                  (println ::join-tournament)
                  (when-not replay?
                    (<p! (.add eventlog
                               (pr-str (merge
                                        {::op.spec/op-key ::app.tournament.chan/join-tournament
                                         ::op.spec/op-type ::op.spec/request-response
                                         ::op.spec/op-orient ::op.spec/request}
                                        {::app.spec/frequency frequency
                                         ::app.spec/peer-name peer-name
                                         ::app.spec/peer-id peer-id})))))
                  (swap! tournament* update-in [::app.spec/peer-metas]
                         assoc peer-id (select-keys value [::app.spec/peer-id
                                                           ::app.spec/peer-name]))
                  (close! out|))


                {::op.spec/op-key ::app.tournament.chan/leave-tournament
                 ::op.spec/op-type ::op.spec/request-response
                 ::op.spec/op-orient ::op.spec/request}
                (let [{:keys [::app.spec/frequency
                              ::app.spec/peer-id
                              ::replay?
                              ::op.spec/out|]} value]
                  (println ::leave-tournament)
                  (when-not replay?
                    (<p! (.add eventlog
                               (pr-str (merge
                                        {::op.spec/op-key ::app.tournament.chan/leave-tournament
                                         ::op.spec/op-type ::op.spec/request-response
                                         ::op.spec/op-orient ::op.spec/request}
                                        {::app.spec/frequency frequency
                                         ::app.spec/peer-name peer-name
                                         ::app.spec/peer-id peer-id})))))
                  (swap! tournament* update-in [::app.spec/peer-metas]
                         dissoc peer-id)
                  (close! out|))

                {::op.spec/op-key ::app.tournament.chan/close-tournament
                 ::op.spec/op-type ::op.spec/request-response
                 ::op.spec/op-orient ::op.spec/request}
                (let [{:keys [::app.spec/frequency
                              ::app.spec/peer-id
                              ::replay?
                              ::op.spec/out|]} value]
                  (println ::close-tournament)
                  (when-not replay?
                    (<p! (.add eventlog
                               (pr-str (merge
                                        {::op.spec/op-key ::app.tournament.chan/close-tournament
                                         ::op.spec/op-type ::op.spec/request-response
                                         ::op.spec/op-orient ::op.spec/request}
                                        {::app.spec/frequency frequency
                                         ::app.spec/peer-name peer-name
                                         ::app.spec/peer-id peer-id})))))
                  (close! out|)))
              (recur)))))
      (println ::go-block-exiting frequency))))