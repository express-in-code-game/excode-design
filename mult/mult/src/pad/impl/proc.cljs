(ns pad.impl.proc
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [goog.string :refer [format]]
   [clojure.string :as string]
   [clojure.pprint :refer [pprint]]

   [pad.protocols.proc :as p.proc]
   [pad.protocols.procs :as p.procs]
   [pad.protocols.proc| :as p.proc|]
   [pad.protocols.procs| :as p.procs|]
   [pad.protocols.channels :as p.channels]
   [pad.impl.channels :as channels]))

(defn proc-interface
  [{:keys [proc|]} lookup]
  (let [proc|i (channels/proc|i)]
    (reify
      p.proc/Proc
      (-start [_]
        (p.proc/-start _ (chan 1)))
      (-start [_ out|]
        (put! proc| (p.proc|/-start proc|i out|))
        out|)
      (-stop [_]
        (p.proc/-stop _ (chan 1)))
      (-stop [_ out|]
        (put! proc| (p.proc|/-stop proc|i out|))
        out|)
      (-running? [_]
        (get @lookup :proc))
      ILookup
      (-lookup [coll k]
        (-lookup coll k nil))
      (-lookup [coll k not-found]
        (-lookup @lookup k not-found)))))

(defn proc-impl
  ([proc-fn ctx]
   (proc-impl (random-uuid) proc-fn ctx))
  ([id proc-fn ctx]
   (let [proc| (chan 10)
         proc-fn| (chan 1)
         lookup (atom {:id id
                       :proc| proc|
                       :proc-fn| proc-fn|
                       :ctx ctx})
         proc|i (channels/proc|i)
         log|i (channels/log|i)]
     (go (loop [state {:proc nil}]
           (swap! lookup merge state)
           (if-let [[v port] (alts! [])]
             (condp = port
               proc| (let [op (p.channels/-op proc|i v)
                           proc-exists? #(some? (get state :proc))
                           explain-proc-exists #(p.channels/-explain log|i [id op] false (format "process %s already exists" id) nil)
                           explain-proc-not-exists #(p.channels/-explain log|i [id op] false (format "process %s does not exist" id) nil)]
                       (condp = op
                         (p.proc|/-op-start proc|i) (let [{:keys [out|]} v]
                                                      (when-let [warning (cond
                                                                           (proc-exists?) (explain-proc-exists))]
                                                        (>! out| warning)
                                                        (recur state))
                                                      (let [p (apply proc-fn (update-in ctx :channels assoc :proc|  proc-fn|))
                                                            o (<! proc-fn|)]
                                                        (>! out| o)
                                                        (recur (update state assoc :proc p))))
                         (p.proc|/-op-stop proc|i) (let [{:keys [out|]} v]
                                                     (when-let [warning (cond
                                                                          (not (proc-exists?)) (explain-proc-not-exists))]
                                                       (>! out| warning)
                                                       (recur state))
                                                     (let [c| (chan 1)
                                                           o (do (>! proc-fn| (p.proc|/-stop proc|i c|))
                                                                 c|)]
                                                       (>! out| o)
                                                       (close! proc-fn|)
                                                       (close! proc|)
                                                       (do nil))))))))
         (println (format "; proc-impl go-block exits, id %" id)))
     (proc-interface {:proc| proc|} lookup))))

(defn procs-interface
  [{:keys [procs| system|]} lookup]
  (let [system|i (channels/system|i)
        procs|i (channels/procs|i)]
    (reify
      p.procs/Procs
      (-start [_ proc-id]
        (let [c| (chan 1)]
          (put! procs| (p.procs|/-start procs|i proc-id c|))
          c|))
      (-stop [_ proc-id]
        (let [c| (chan 1)]
          (put! procs| (p.procs|/-stop procs|i proc-id c|))
          c|))
      (-restart [_ proc-id]
        (let [c| (chan 1)]
          (put! procs| (p.procs|/-restart procs|i proc-id c|))
          c|))
      (-up [th ctx]
        (let [c| (chan 1)]
          (put! procs| (p.procs|/-up procs|i th ctx c|))
          c|))
      (-down [th]
        (let [c| (chan 1)]
          (put! procs| (p.procs|/-down procs|i th nil c|))
          c|))
      (-downup [th ctx]
        (let [c| (chan 1)]
          (go
            (<! (p.procs/-down th))
            (<! (p.procs/-up th ctx)))
          c|))
      (-up? [_]
        (:up? @lookup))
      ILookup
      (-lookup [coll k]
        (-lookup coll k nil))
      (-lookup [coll k not-found]
        (-lookup @lookup k not-found)))))

(defn procs-impl
  [opts]
  (let [{procs-map :procs
         up :up
         down :down
         ctx :ctx} opts
        system| (get-in ctx [:channels :system|])
        procs| (chan 10)
        procs|m (mult procs|)
        procs|t (tap procs|m (chan 10))
        procs|i (channels/procs|i)
        system|i (channels/system|i)
        log|i (channels/log|i)
        lookup (atom {:opts opts
                      :procs| procs|
                      :procs|m procs|m
                      :up? nil
                      :procs nil
                      :ctx ctx})]
    (go (loop [state {:ctx ctx
                      :up? false
                      :procs {}}]
          (swap! lookup merge state)
          (println "; procs-impl loop")
          (try
            (if-let [[v port] (alts! [procs|t])]
              (condp = port
                procs|t (let [op (p.channels/-op procs|i v)
                              procs (:procs state)
                              map-missing? #(not (contains? procs-map %))
                              cotext-missing? #(not (get state :ctx))
                              proc-exists? #(contains? (get state :procs) %)
                              explain-context-missing #(p.channels/-explain log|i % false (format "ctx is missing") %)
                              explain-map-missing #(p.channels/-explain log|i % false (format "process %s is not in the procs-map" %) %)
                              explain-proc-exists #(p.channels/-explain log|i % false (format "process %s already exists" %) %)
                              explain-proc-not-exists #(p.channels/-explain log|i % false (format "process %s does not exist" %) %)]
                          (condp = op
                            (p.procs|/-op-start procs|i) (let [{:keys [proc/id out|]} v]
                                                           (when-let [warning (cond
                                                                                (cotext-missing?) (explain-context-missing id)
                                                                                (map-missing? id) (explain-map-missing id)
                                                                                (proc-exists? id) (explain-proc-exists id))]
                                                             (>! out| warning)
                                                             (recur state))
                                                           (let [{:keys [proc-fn ctx-fn]} (get procs-map id)
                                                                 p (proc-impl id proc-fn (ctx-fn ctx))]
                                                             (take! (p.proc/-start p) (fn [o]
                                                                                        (put! out| o)
                                                                                        (put! procs| (p.procs|/-started procs|i id))))
                                                             (recur (update-in state [:procs] assoc id {:proc p :status :starting}))))
                            (p.procs|/-op-started procs|i) (let [{:keys [proc/id]} v]
                                                             (offer! system| (p.channels/-proc-started system|i id v))
                                                             (recur (update-in state [:procs id] assoc :status :started)))
                            (p.procs|/-op-stop procs|i) (let [{:keys [proc/id out|]} v]
                                                          (when-let [warning (cond
                                                                               (not (proc-exists? id)) (explain-proc-not-exists id))]
                                                            (>! out| warning)
                                                            (recur state))
                                                          (let [p (get @procs id)]
                                                            (take! (p.proc/-stop p) (fn [o]
                                                                                      (put! out| o)
                                                                                      (put! procs| (p.procs|/-stopped procs|i id))))
                                                            (recur (update-in state [:procs id] assoc :status :stopping))))
                            (p.procs|/-op-stopped procs|i) (let [{:keys [proc/id]} v]
                                                             (offer! system| (p.channels/-proc-stopped system|i id v))
                                                             (recur (update-in state [:procs] dissoc  id)))
                            (p.procs|/-op-error procs|i) (let [{:keys [proc/id]} v]
                                                           (offer! system| (p.channels/-proc-stopped system|i id v))
                                                           (recur (update-in state [:procs] dissoc id)))
                            (p.procs|/-op-restart procs|i) (let [{:keys [proc/id out|]} v]
                                                             (when-let [warning (cond
                                                                                  (map-missing? id) (explain-map-missing id))]
                                                               (>! out| warning)
                                                               (recur state))
                                                             (let [c| (chan 1)]
                                                               (put! procs| (p.procs|/-stop procs|i id c|))
                                                               (take! c| (fn [v]
                                                                           (put! procs| (p.procs|/-start procs|i id out|))))))
                            (p.procs|/-op-up procs|i) (let [{:keys [procs ctx out|]} v
                                                            o (<! (up procs ctx))]
                                                        (>! out| o)
                                                        #_(>! system| (-procs-up system|i k v))
                                                        (recur (update state merge {:up? true
                                                                                    :ctx ctx})))
                            (p.procs|/-op-down procs|i) (let [{:keys [procs ctx out|]} v
                                                              o (<! (down procs ctx))]
                                                          #_(>! system| (-procs-down system|i k v))
                                                          (>! out| o)
                                                          (recur (update state merge {:up? false}))))
                          (recur state))))
            (catch js/Error e (do (println "procs-impl exception:")
                                  (println e))))
          (recur state))
        (println "; procs-impl go-block exits"))
    (procs-interface {:procs| procs|
                      :system| system|} lookup)))


; repl only
(def ^:private logs (atom {}))

(defn proc-log
  [{:keys [proc| log|m]} ctx]
  (let [log|t (tap log|m (chan 100))]
    (go (loop [state {:log []}]
          (reset! logs state)
          (if-let [[v port] (alts! [log|t])]
            (condp = port
              log|t (let []
                      (pprint v)
                      (recur (update-in state [:log] conj v))))))
        (println "; proc-log go-block exits")
        )))


#_(def procs (proc/procs-impl
              {:ctx {:channels channels}
               :procs {:proc-ops {:proc-fn #'proc-ops
                                  :ctx-fn identity
                                  #_(fn [ctx]
                                      (-> % (select-keys [:channels  :editor-ctx])
                                          (update :channels #(select-keys % [:cmd| :ops|]))))}
                       :proc-log {:proc-fn #'proc/proc-log
                                  :ctx-fn identity}
                       :proc-editor {:proc-fn #'editor/proc-editor
                                     :ctx-fn identity}}
               :proc-main #'proc-main}))