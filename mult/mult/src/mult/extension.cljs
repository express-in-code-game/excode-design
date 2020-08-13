(ns mult.extension
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [goog.string :refer [format]]
   [clojure.string :as string]
   [cljs.reader :refer [read-string]]
   [clojure.pprint :refer [pprint]]

   #_[pad.cljsjs1]
   #_[pad.selfhost1]

   [mult.protocols :as p]
   [mult.impl.editor :as editor]
   [mult.impl.channels :as channels]
   [mult.impl.conn :as conn]
   [mult.impl.lrepl :as lrepl]
   [mult.impl.conf :as conf]
   #_[mult.impl.self-hosting :as self-hosting]))

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
                 :editor|m editor|m
                 }))

(declare proc-main proc-ops proc-log)


(defn main [])

; repl only
(def proc-main-state (atom {}))

(defn proc-main
  [channels ctx]
  #_(do
      (prn (pad.cljsjs1/test1)))
  (let [pid [:proc-main]
        {:keys [main| main|m log| ops|]} channels
        main|t (tap main|m (chan 10))
        main|i (channels/main|i)
        ops|i (channels/ops|i)
        log|i (channels/log|i)
        log (fn [& args] (put! log| (apply p/-vl-log log|i args)))]
    (go (loop [state {:channels channels
                      :ctx ctx
                      :procs {}
                      :editor nil
                      :activated? false}]
          (reset! proc-main-state state)
          (try
            (if-let [v (<! main|t)]
              (condp = (p/-op main|i v)
                (p/-op-init main|i) (let [{:keys [channels ctx]} state]
                                      (do
                                        (proc-log channels ctx)))
                (p/-op-proc-started main|i) (let [{:keys [proc-id proc|]} v]
                                              #_(log (format "; process started: %s" proc-id))
                                              (recur (-> state
                                                         (update-in [:procs] assoc proc-id proc|))))
                (p/-op-proc-stopped main|i) (let [{:keys [proc-id]} v]
                                              #_(log (format "; process stopped: %s" proc-id))
                                              (recur (-> state
                                                         (update-in [:procs] dissoc proc-id))))
                #_(p/-op-start-proc main|i) #_(let [{:keys [proc-fn]} v
                                                    {:keys [channels ctx]} state]
                                                (log (format "; process starting: %s" proc-id))
                                                (proc-fn channels ctx))
                (p/-op-stop-proc main|i) (let [{:keys [proc-id]} v
                                               chans (-> (get state :procs)
                                                         (keep (fn [[k v]] (when (= (first k) proc-id) v))))]
                                           (if (empty? chans)
                                             (log (format "; could not stop process, no such proc-id: %s" proc-id))
                                             (doseq [c| chans]
                                               #_(log (format "; process stopping: %s" proc-id))
                                               (put! c| v))))
                (p/-op-restart-proc main|i) (let [{:keys [proc-id]} v]
                                              (>! main| (p/-vl-stop-proc main|i proc-id))
                                              (>! main| (p/-vl-start-proc main|i proc-id)))
                (p/-op-activate main|i) (let [{:keys [editor-context]} v
                                              ctx' (assoc (:ctx state) :editor-context  editor-context)
                                              editor (editor/editor channels ctx')
                                              state' (-> state
                                                         (assoc :ctx  ctx')
                                                         (assoc :editor  editor))]
                                          (when-not (:activated? state)
                                            #_(<! (self-hosting/init (p/-join-workspace-path editor "resources/out/bootstrap")))
                                            (proc-ops (:channels state') (:ctx state') editor)
                                            (>! ops| (p/-vl-activate ops|i))
                                            (recur state')))
                (p/-op-deactivate main|i) (let []
                                            (when (:activated? state)
                                              (>! ops| (p/-vl-deactivate ops|i))
                                              (>! main| (p/-vl-stop-proc main|i :proc-ops))
                                              (p/-release (:editor state))
                                              (recur (-> state
                                                         (assoc  :activated? false)
                                                         (assoc  :editor nil))))))
              (recur state))
            (catch js/Error e (do (println "; proc-main error, will resume")
                                  (println e))))
          (recur state))
        (log "; proc-main go-block exiting, but it shouldn't"))))

(defn default-commands
  []
  ["mult.open"
   "mult.ping"
   "mult.eval"])

; repl only
(def ^:private proc-ops-state (atom {}))

(defn proc-ops
  [channels ctx editor]
  (let [pid [:proc-ops (random-uuid)]
        {:keys [main| log| ops|m cmd|m conn| conn-status|m conn-status|x]} channels
        proc| (chan 1)
        ops|t (tap ops|m (chan 10))
        cmd|t (tap cmd|m (chan 100))
        conn-status|t (tap conn-status|m (chan (sliding-buffer 10)))
        main|i (channels/main|i)
        ops|i (channels/ops|i)
        tab|i (channels/tab|i)
        log|i (channels/log|i)
        cmd|i (channels/cmd|i)
        conn|i (channels/netsock|i)
        log (fn [& args] (put! log| (apply p/-vl-log log|i args)))
        release #(do
                   (untap ops|m ops|t)
                   (untap cmd|m cmd|t)
                   (untap conn-status|m conn-status|t)
                   (close! ops|t)
                   (close! cmd|t)
                   (close! conn-status|t)
                   (close! proc|)
                   (put! main| (p/-vl-proc-stopped main|i pid)))]
    (put! main| (p/-vl-proc-started main|i pid proc|))
    (go (loop [state {:conf nil
                      :conf* nil
                      :tabs {}
                      :conns {}
                      :lrepls {}
                      :mult.edn nil}]
          (reset! proc-ops-state state)
          (try
            (if-let [[v port] (alts! [cmd|t ops|t conn-status|t proc|])]
              (condp = port
                proc| (release)
                conn-status|t (let [op (p/-op conn|i v)]
                                (condp = op
                                  (p/-op-connected conn|i) (let [{:keys [id]} (:opts v)]
                                                             (log (format "%s connected" id)))
                                  (p/-op-ready conn|i) (let [{:keys [id]} (:opts v)]
                                                         (log (format "%s ready" id)))
                                  (p/-op-disconnected conn|i) (let [hadError (:hadError (:opts v))
                                                                    {:keys [id]} (:opts v)]
                                                                (log (format "%s disconnected, hadError %s" id hadError))
                                                                (recur (update state :conns dissoc id)))
                                  (p/-op-timeout conn|i) (let [{:keys [id]} (:opts v)]
                                                           (log (format "%s timeout" id))
                                                           (recur (update state :conns dissoc id)))
                                  (p/-op-error conn|i) (let [err (:err (:opts v))
                                                             {:keys [id]} (:opts v)]
                                                         (log (format "%s error" id) err)
                                                         (recur (update state :conns dissoc id))))
                                (recur state))
                ops|t (let [op (p/-op ops|i v)]
                        (condp = op
                          (p/-op-activate ops|i) (do
                                                   (p/-register-commands editor (default-commands))
                                                   (p/-show-info-msg editor "mult actiavting"))
                          (p/-op-deactivate ops|i) (p/-show-info-msg editor "deactiavting")
                          (p/-op-tab-disposed ops|i) (let [{:keys [tab/id]} v]
                                                       (log (format "tab  %s disposed" id)))
                          (p/-op-connect ops|i) (let [{:keys [id]} v
                                                      data (get-in (:conf* state) [:connections id])
                                                      conn (conn/nrepl {:id id
                                                                        :host (:host data)
                                                                        :port (:port data)} log)]
                                                  (admix conn-status|x (:status| conn))
                                                  (p/-connect conn)
                                                  (recur (update state :conns assoc id conn)))
                          (p/-op-disconnect ops|i) (let [{:keys [host port id]} v
                                                         conn (get-in state [:conns id])]
                                                     (unmix conn-status|x (:status| conn))
                                                     (p/-disconnect conn))
                          (p/-op-texteditor-changed ops|i) (let [{:keys [filepath ns-sym]} (:data v)
                                                                 tab-ids (conf/filepath->tab-ids (:conf* state) filepath)
                                                                 lrepl-ids (conf/filepath->lrepl-ids (:conf* state) filepath)
                                                                 tabs (select-keys (:tabs state) tab-ids)
                                                                 data (merge (:data v) {:lrepl-id (first lrepl-ids)})]
                                                             (doseq [[id tab] tabs]
                                                               (p/-send tab (p/-vl-namespace-changed tab|i data)))))
                        (recur state))
                cmd|t (let [cmd (:cmd/id v)]
                        (condp = cmd
                          "mult.open" (let [conf (-> (<! (p/-read-workspace-file editor ".vscode/mult.edn"))
                                                     (read-string))
                                            conf* (conf/evaluate conf)
                                            conns (reduce (fn [ag [conn-id data]]
                                                            (let [conn (conn/nrepl {:id conn-id
                                                                                    :host (:host data)
                                                                                    :port (:port data)} log)]
                                                              (assoc ag conn-id conn)))
                                                          {} (:connections conf))
                                            tabs (reduce (fn [ag tab-id]
                                                           (assoc ag tab-id (p/-create-tab editor tab-id)))
                                                         {} (:tabs/active conf))
                                            lrepls (reduce (fn [ag [lrepl-id data]]
                                                             (assoc ag lrepl-id (lrepl/lrepl (:iden data))))
                                                           {} (:repls conf))]
                                        (doseq [[conn-id conn] conns]
                                          (admix conn-status|x (:status| conn))
                                          (p/-connect  conn))
                                        (doseq [[tab-id tab] tabs]
                                          (p/-send tab (p/-vl-conf tab|i conf)))
                                        #_(p/-show-info-msg editor "mult.open")
                                        (recur (merge state {:conf conf
                                                             :conf* conf*
                                                             :lrepls lrepls
                                                             :conns conns
                                                             :tabs tabs})))
                          "mult.ping" (do
                                        (p/-show-info-msg editor "mult.ping via channels")
                                        (<! (timeout 3000))
                                        (p/-show-info-msg editor "mult.ping via channels later"))
                          "mult.eval"  (when-let [{:keys [filepath ns-sym]} (p/-active-ns editor)]
                                         (let [lrepl-ids (conf/filepath->lrepl-ids (:conf* state) filepath)
                                               lrepl-id (first lrepl-ids)
                                               lrepl (get (:lrepls state) lrepl-id)
                                               conn-id (get-in (:conf* state) [:repls lrepl-id :conn])
                                               conn (get (:conns state) conn-id)
                                               tab-ids (conf/lrepl-id->tab-ids (:conf* state) lrepl-id)
                                               tabs (select-keys (:tabs state) tab-ids)
                                               selection (p/-selection editor)]
                                           (try
                                             (let [data (<! (p/-eval lrepl conn selection ns-sym))]
                                               (doseq [[id tab] tabs]
                                                 (p/-send tab (p/-vl-tab-append tab|i data))))
                                             (catch js/Error ex (log "; error when evaluating" ex))))))
                        (recur state))))
            (catch js/Error e (do (log "; proc-ops error, will exit" e)))
            (finally
              (release))))
        (println "; proc-ops go-block exits"))))

(reduce (fn [ag x] (assoc ag x x)) {} #{1 2 3})

; repl only
(def ^:private proc-log-state (atom {}))

(defn proc-log
  [channels ctx]
  (let [pid [:proc-log (random-uuid)]
        {:keys [main| main|m log| log|m  editor|m cmd|m conn-status|m]} channels
        proc| (chan 1)
        main|t (tap main|m (chan 10))
        log|t (tap log|m (chan 10))
        editor|t (tap editor|m (chan 10))
        cmd|t (tap cmd|m (chan 100))
        conn-status|t (tap conn-status|m (chan 100))
        main|i (channels/main|i)
        append #(-> %1
                    (update-in [:log] conj %2)
                    (update-in [:log] (partial take-last 50)))
        release #(do
                    (untap main|m main|t)
                    (untap log|m log|t)
                    (untap editor|m  editor|t)
                    (untap cmd|m  cmd|t)
                    (untap conn-status|m conn-status|t)
                    (close! main|t)
                    (close! log|t)
                    (close! editor|t)
                    (close! cmd|t)
                    (close! conn-status|t)
                    (close! proc|)
                    (put! main| (p/-vl-proc-stopped main|i pid)))]
    (put! main| (p/-vl-proc-started main|i pid proc|))
    (go (loop [state {:log []}]
          (reset! proc-log-state state)
          (try
            (if-let [[v port] (alts! [log|t main|t editor|t cmd|t proc|])]
              (cond
                (= port proc|) (release)
                :else (let [printable (condp = port
                                        log|t (let [{:keys [id comment data]} v]
                                                {:comment comment
                                                 :data data})
                                        main|t (let []
                                                 v)
                                        editor|t (let []
                                                   v)
                                        cmd|t (let []
                                                v)
                                        conn-status|t (let []
                                                        (select-keys v [:op :id :err :hadError])))]
                        (pprint printable)
                        (recur (append state v)))))
            (catch js/Error e (do (println "; proc-log error, will exit")
                                  (println e)))
            (finally
              (release))))
        (println "; proc-log go-block exits"))))

(defonce _ (let [main|i (channels/main|i)]
             (put! (channels :main|) (p/-vl-init main|i))
             (proc-main channels {})))

(def activate (let [main|i (channels/main|i)]
                (fn [context]
                  (put! (channels :main|) (p/-vl-activate main|i context)))))
(def deactivate (let [main|i (channels/main|i)]
                  (fn []
                    (put! (channels :main|) (p/-vl-deactivate main|i)))))

(def exports #js {:activate activate
                  :deactivate deactivate})

(when (exists? js/module)
  (set! js/module.exports exports))




#_(defn reload
    []
    (.log js/console "Reloading...")
    (js-delete js/require.cache (js/require.resolve "./main")))
