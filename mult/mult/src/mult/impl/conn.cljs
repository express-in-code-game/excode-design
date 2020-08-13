(ns mult.impl.conn
  (:require
   [clojure.core.async :as a :refer [<! >!  chan go alt! take! put! offer! poll! alts! pub sub unsub
                                     timeout close! to-chan  mult tap untap mix admix unmix
                                     pipeline pipeline-async go-loop sliding-buffer dropping-buffer]]
   [goog.string :refer [format]]
   [clojure.string :as string]
   [clojure.pprint :refer [pprint]]
   [cljs.reader :refer [read-string]]
   [mult.protocols :as p]
   [mult.impl.channels :as channels]
   [mult.impl.async :as mult.async]
   [cljs.nodejs :as node]))

(def path (node/require "fs"))
(def fs (node/require "path"))
(def net (node/require "net"))
(def bencode (node/require "bencode"))
(def nrepl-client (node/require "nrepl-client"))

(defn netsocket
  [opts]
  (let [{:keys [reconnection-timeout id]
         :or {reconnection-timeout 1000}} opts
        status| (chan (sliding-buffer 10))
        send| (chan (sliding-buffer 1))
        receive| (chan (sliding-buffer 10))
        receive|m (mult receive|)
        netsock|i (channels/netsock|i)
        socket (net.Socket.)
        connect #(.connect socket (clj->js (select-keys opts [:host :port])))
        socket (doto socket
                 (.on "connect" (fn [] (put! status| (p/-vl-connected netsock|i opts))))
                 (.on "ready" (fn [] (put! status| (p/-vl-ready netsock|i opts))))
                 (.on "timeout" (fn [] (put! status| (p/-vl-timeout netsock|i  opts))))
                 (.on "close" (fn [hadError]
                                (when (and (not socket.connecting) (not socket.pending))
                                  (put! status| (p/-vl-disconnected netsock|i hadError opts)))
                                ; consider using take! if using go block here is wasteful
                                (go
                                  (<! (timeout reconnection-timeout))
                                  (cond
                                    socket.connecting (do nil)
                                    socket.pending (do (connect))
                                    :else (do nil)))))
                 (.on "error" (fn [err] (put! status| (p/-vl-error netsock|i err opts))))
                 (.on "data" (fn [buf] (put! receive| buf))))
        lookup (merge opts {:status| status|
                            :send| send|
                            :receive|m receive|m})
        conn (reify
               cljs.core/ILookup
               (-lookup [_ k] (-lookup _ k nil))
               (-lookup [_ k not-found] (-lookup lookup k not-found))
               p/Connect
               (-connect [_] (connect))
               (-disconnect [_] (.end socket))
               (-connected? [_] (not socket.pending) #_(not socket.connecting))
               p/Send
               (-send [_ v] (.write socket v))
               p/Release
               (-release [_] (close! send|)))
        release #(do
                   (p/-disconnect conn)
                   (close! send|)
                   (close! receive|)
                   (close! status|))]
    (go-loop []
      (when-let [v (<! send|)]
        (p/-send conn v)
        (recur))
      (release))
    conn))

(defn nrepl
  [opts log]
  (let [proc| (chan 10)
        {:keys [id]} opts
        socket (netsocket (select-keys opts [:reconnection-timeout :host :port :id]))
        {:keys [send| receive|m status|]} socket
        topic-fn :id
        xf-send #(.encode bencode (clj->js %))
        xf-receive #(as-> % v
                      (.toString v)
                      (.decode bencode v "utf8")
                      (js->clj v :keywordize-keys true))
        receive|t (tap receive|m (chan (sliding-buffer 10)))
        nrepl| (chan (sliding-buffer 10))
        nrepl|p (mult.async/pub nrepl| topic-fn (fn [_] (sliding-buffer 10)))
        lookup (merge opts {:status| status|})]
    (go-loop []
      (when-let [v (<! receive|t)]
        (try
          (let [d (xf-receive v)]
            (prn "receive")
            (prn d)
            (when (:id d)
              (>! nrepl| d)))
          (catch js/Error ex))
        (recur))
      (log (format "nrepl %s xform process exiting" id)))
    (reify
      p/Connect
      (-connect [_] (p/-connect socket))
      (-disconnect [_] (p/-disconnect socket))
      (-connected? [_] (p/-connected? socket))
      p/ReplConn

      (-describe [_  opts])
      (-interrupt [_ session opts])
      (-ls-sessions [_])
      (-nrepl-op [_ opts]
        (let [{:keys [done-keys op-data result-keys]
               :or {done-keys [:status :err]
                    result-keys [:value :err]}} opts
              topic (str (random-uuid))
              nrepl|s (chan 10)
              _ (sub nrepl|p topic nrepl|s)
              res| (chan 50)
              release #(do
                         (close! res|)
                         (close! nrepl|s)
                         (unsub nrepl|p topic nrepl|s)
                         (mult.async/close-topic nrepl|p topic))
              ex| (chan 1)
              req (merge op-data {:id topic})]
          (try
            (prn "sending")
            (prn req)
            (put! send| (xf-send req))
            (catch js/Error ex (put! ex| {:comment "Error xfroming/sending nrepl op" :err-str (str ex)})))
          (go
            (loop [t| (timeout 10000)]
              (alt!
                nrepl|s ([v] (when v
                               (>! res| v)
                               (if (not-empty (select-keys v done-keys))
                                 (do (release)
                                     (let [res (<! (a/into [] res|))]
                                       (transduce
                                        (comp
                                         (keep #(or (not-empty (select-keys % result-keys)) nil))
                                         #_(mapcat vals))
                                        merge
                                        {:req  req
                                         :res res}
                                        res)))
                                 (recur t|))))
                t| ([v] (do
                          (release)
                          {:comment "Error: -nrepl-op timed out" :req req}))
                ex| ([ex] (do
                            (release)
                            ex)))))))
      (-close-session [_ session opts])
      (-clone-session [_]
        (let []
          (p/-nrepl-op _ (merge opts {:op-data {:op "clone"} :result-keys [:new-session]}))))
      p/Eval
      (-eval [_ opts]
        (let [{:keys [code  session]} opts]
          (p/-nrepl-op _ (merge opts {:op-data {:op "eval" :code code :session session}}))))
      cljs.core/ILookup
      (-lookup [_ k] (-lookup _ k nil))
      (-lookup [_ k not-found] (-lookup lookup k not-found)))))

(comment

  (as-> {:op :eval :id (str (random-uuid)) :code "(+ 1 2)"} x
    (.encode bencode (clj->js x))
    (.decode bencode x "utf8")
    (js->clj x :keywordize-keys true))


  (do
    (def s (netsocket {:host "localhost" :port 7788
                       :topic-fn #(get-in % [:data :id])
                       :xf-send #(.encode bencode (clj->js %))
                       :xf-msg #(as-> % v
                                  (.toString v)
                                  (.decode bencode v "utf8")
                                  (js->clj v :keywordize-keys true))}))
    (go-loop []
      (when-let [v (<! (:status| s))]
        (println v)
        (recur)))
    #_(go-loop [c| (tap (:msg|m s) (chan 10))]
        (when-let [v (<! c|)]
          (println "value from msg|m")
          (println v)
          (recur c|)))
    (p.conn/-connect s))



  (p.conn/-disconnect s)

  (def nr (nrepl))
  (take! (p.conn/-eval nr "(+ 1 2)" nil (select-keys s [:msg|p :send|])) prn)

  (take! (:status| s) prn)
  (take! (tap (:msg|m s) (chan 1)) prn)

  ;;
  )


(comment

  (do
    (def c (.connect nrepl-client #js {:port 8899
                                       :host "localhost"}))
    (doto c
      (.on "connect" (fn []
                       (println "; net/Socket connect")))
      (.on "ready" (fn []
                     (println "; net/Socket ready")))
      (.on "timeout" (fn []
                       (println "; net/Socket timeout")))
      (.on "close" (fn [hadError]
                     (println "; net/Socket close")
                     (println (format "hadError %s"  hadError))))
      (.on "error" (fn [e]
                     (println "; net/Socket error")
                     (println e)))))

  (.end c)


  (def code "shadow.cljs.devtools.api/compile")
  (.eval c "conj" (fn [err result]
                    (println ".eval data")
                    (println (or err result))))
  (.lsSessions c (fn [err data]
                   (println ".lsSessions data")
                   (println data)))

  (.describe c (fn [err data]
                 (println ".describe data")
                 (println messages)))


  ;;
  )