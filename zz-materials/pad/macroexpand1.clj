(ns pad.macroexpand1
  (:require
   [clojure.core.async :as a :refer [<! >! <!! timeout chan alt! go close!
                                     >!! <!! alt!! alts! alts!! take! put! mult tap untap
                                     thread pub sub sliding-buffer mix admix unmix]]
   [clojure.walk]
   [clojure.spec.alpha :as s]
   [clojure.test.check.generators :as gen]))

(defprotocol Mountable
  :extend-via-metadata true
  (mount* [_] [_ opts])
  (unmount* [_] [_ opts]))

(def ^:const OP :op)

(s/def ::out| any?)

(def ops
  {::mount (s/keys :req-un [::op #_::out|])
   ::unmount (s/keys :req-un [::op #_::out|])})

(def ops-keys (set (keys ops)))
(s/def ::op ops-keys)

(def channels
  {:main/ops| #{::mount ::unmount}
   :game| #{}
   :render/ops| #{}})


(def channels-keys (set (keys channels)))

;; (defmulti op-type OP)

;; (defmethod op-type :app.main/mount
;;   [vl]
;;   (s/keys :req [::op ::out|]))

;; (s/def ::op (s/multi-spec op-type OP))
;; 

(s/def ::channel-exists channels-keys)
(s/def ::op-exists #(ops-keys (if (keyword? %) % (OP %))))
(s/def ::channel-op-exists (fn [{:keys [channel-key val-map op-key]}]
                             ((channel-key channels) (or op-key (OP val-map)))))
(s/def ::val-map-valid (fn [{:keys [channel-key val-map]}]
                         (s/valid? ((OP val-map) ops) val-map)))

#_(s/valid? (::mount ops) {:op ::mount})
#_(gen/generate (s/gen (::mount ops)))

(defmacro opr
  [channel-key op-key]
  `~op-key)

(s/fdef opr
  :args (s/and
         (s/cat :channel-key ::channel-exists
                :op-key ::op-exists)
         ::channel-op-exists)
  :ret any?)

(defmacro vl
  [channel-key val-map]
  `~val-map)

(s/fdef vl
  :args (s/and
         (s/cat :channel-key ::channel-exists
                :val-map ::op-exists)
         ::channel-op-exists
         ::val-map-valid)
  :ret any?)


;; works
#_(defn f1
    [channels ctx]
    (let [ops| (chan 10)
          loop| (reify Mountable
                  (mount* [_]
                    (loop []
                      (when-let [{:keys [op opts out|]} ops|]
                        (condp = op
                          (opr :main/ops| ::mount) (let [exts {:project.ext/scenarios (project.ext.scenarios.main/mount channels ctx)
                                                               :project.ext/connect (project.ext.connect.main/mount channels ctx)
                                                               :project.ext/server (project.ext.server.main/mount channels ctx)
                                                               :project.ext/games (project.ext.games.main/mount channels ctx)}]
                                                     (prn ::mount)
                                                     (swap! ctx update :exts merge exts)
                                                     (put! out| true)
                                                     (close! out|))
                          (opr :main/ops| ::unmount) (future (let []
                                                               (prn ::unmount)))))
                      (recur))))]
      (with-meta
        {:loop| loop|}
        {'Mountable '_
         `mount* (fn [_ opts] (put! ops| (vl :main/ops| {:op ::mount})))
         `unmount* (fn [_ opts] (put! ops| {:op ::unmount}))})))

;; does not work
(defn f2
  []
  (go
    (opr :main/ops| ::mount1)))

;; works
#_(defn f3
    []
    (a/go-loop []
      (opr :main/ops| ::mount1)))

;; does not work
(defn f4
  []
  (a/go
    (opr :main/ops| ::mount1)))

(defmacro go2
  [& body]
  `(go ~@body))

;; does not work
(defn f4
  []
  (go2
   (opr :main/ops| ::mount1)))

;; works
#_(defn f5
    [channels ctx]
    (let [ops| (chan 10)
          loop| (a/go-loop []
                  (opr :main/ops| ::mount1)
                  (opr :main/ops| ::unmount1)
                  (recur))]))

;; does not work
(defn f5-2
  [channels ctx]
  (let [ops| (chan 10)
        loop| (a/go-loop []
                (opr :main/ops| ::mount1)
                (opr :main/ops| ::unmount1)
                (when-let [{:keys [op opts out|]} (<! ops|)])
                (recur))]))

;; does not work
(defn f5-2-2
  [ops|]
  (a/go-loop []
    (opr :main/ops| ::mount1)
    (<! ops|)))

;; works
#_(defn f5-2-3
    [ops|]
    (a/go-loop []
      (opr :main/ops| ::mount1)
      (do ops|)))

;; WORKS
#_(defn f5-2-4
    [ops|]
    (go (loop []
          (opr :main/ops| ::mount1)
          (do ops|))))

;; WORKS
#_(defn f5-2-5
    [ops|]
    (go (let []
          (opr :main/ops| ::mount1)
          (do ops|))))

;; WORKS
#_(defn f5-2-5-2
    [ops|]
    (go (let []
          (prn (opr :main/ops| ::mount1))
          (do ops|))))

;; does not work
(defn f5-2-6
  [ops|]
  (go (let []
        (opr :main/ops| ::mount1)
        (<! ops|))))

(comment
  (macroexpand '(go (let []
                      (opr :main/ops| ::mount1)
                      (<! (chan 1)))))

  (macroexpand '(go (let []
                      (opr :main/ops| ::mount1)
                      (do (chan 1)))))

  (macroexpand-1 '(go (let []
                        (opr :main/ops| ::mount1)
                        (do (chan 1)))))

  ;; works
  (clojure.walk/macroexpand-all '(go (let []
                                       (prn (opr :main/ops| ::mount1))
                                       (do (chan 1)))))

  ;; works 
  (clojure.walk/macroexpand-all '(go (let []
                                       (prn (opr :main/ops| ::mount1))
                                       (<! (chan 1)))))


  ;; does not work: should throw, but it expands
  (clojure.walk/macroexpand-all '(go (loop []
                                       (prn (opr :main/ops| ::mount2)) ; gets expanded into (clojure.core/prn :pad.macroexpand1/mount2), but does not throw! why ?
                                       (when-let [{:keys [op opts out|]} (<! (chan 1))]
                                         (condp = op
                                           (opr :main/ops| ::mount1) (let [exts {}
                                                                           ctx (atom {})]
                                                                       (prn ::mount)
                                                                       (swap! ctx update :exts merge exts)
                                                                       (put! out| true)
                                                                       (close! out|))
                                           (opr :main/ops| ::unmount) (future (let []
                                                                                (prn ::unmount)))))
                                       (recur))
                                     (println ";; proc-main exiting")))

  ;; works! what's the diff ?
  ;; (clojure.core/prn :pad.macroexpand1/mount2) vs (prn :pad.macroexpand1/mount) ?
  (clojure.walk/macroexpand-all '(go (loop []
                                       (prn (opr :main/ops| ::mount2)))))

  ;; also works
  (clojure.walk/macroexpand-all '(go (prn (opr :main/ops| ::mount2))))

  ;; does not work; why ?
  (clojure.walk/macroexpand-all '(go (opr :main/ops| ::mount2)))
  (clojure.walk/macroexpand-all '(go (do (opr :main/ops| ::mount2))))
  
  ;;
  )

;; does not work
(defn proc-main-f
  [channels ctx]
  (let [ops| (chan 10)
        loop| (go (loop []
                    (when-let [{:keys [op opts out|]} (<! ops|)]
                      (condp = op
                        (opr :main/ops| ::mount1) (let [exts {}]
                                                    (prn ::mount)
                                                    (swap! ctx update :exts merge exts)
                                                    (put! out| true)
                                                    (close! out|))
                        (opr :main/ops| ::unmount) (future (let []
                                                             (prn ::unmount)))))
                    (recur))
                  (println ";; proc-main exiting"))]))


;; does not work
(defn f5-3
  [channels ctx]
  (let [ops| (chan 10)
        loop| (a/go-loop []
                (opr :main/ops| ::mount1)
                (when-let [{:keys [op opts out|]} (<! ops|)]
                  (condp = op
                    (opr :main/ops| ::mount1) (let [exts {:project.ext/scenarios (project.ext.scenarios.main/mount channels ctx)
                                                          :project.ext/connect (project.ext.connect.main/mount channels ctx)
                                                          :project.ext/server (project.ext.server.main/mount channels ctx)
                                                          :project.ext/games (project.ext.games.main/mount channels ctx)}]
                                                (prn ::mount)
                                                (swap! ctx update :exts merge exts)
                                                (put! out| true)
                                                (close! out|))
                    (opr :main/ops| ::unmount) (future (let []
                                                         (prn ::unmount)))))
                (recur))]))


(comment

  (macroexpand '(go (let []
                      (prn (opr :main/ops| ::mount1))
                      (<! (chan 1)))))
  (let*
   [c__6964__auto__ (clojure.core.async/chan 1) captured-bindings__6965__auto__ (clojure.lang.Var/getThreadBindingFrame)]
   (clojure.core.async.impl.dispatch/run
    (fn*
     []
     (clojure.core/let
      [f__6966__auto__
       (clojure.core/fn
         state-machine__6709__auto__
         ([]
          (clojure.core.async.impl.ioc-macros/aset-all!
           (java.util.concurrent.atomic.AtomicReferenceArray. 7)
           0
           state-machine__6709__auto__
           1
           1))
         ([state_20202]
          (clojure.core/let
           [old-frame__6710__auto__
            (clojure.lang.Var/getThreadBindingFrame)
            ret-value__6711__auto__
            (try
              (clojure.lang.Var/resetThreadBindingFrame (clojure.core.async.impl.ioc-macros/aget-object state_20202 3))
              (clojure.core/loop
               []
                (clojure.core/let
                 [result__6712__auto__
                  (clojure.core/case
                   (clojure.core/int (clojure.core.async.impl.ioc-macros/aget-object state_20202 1))
                    1
                    (clojure.core/let
                     [inst_20197
                      (prn (opr :main/ops| :pad.go1/mount1))
                      inst_20198
                      (chan 1)
                      state_20202
                      (clojure.core.async.impl.ioc-macros/aset-all! state_20202 6 inst_20197)]
                      (clojure.core.async.impl.ioc-macros/take! state_20202 2 inst_20198))
                    2
                    (clojure.core/let
                     [inst_20200 (clojure.core.async.impl.ioc-macros/aget-object state_20202 2)]
                      (clojure.core.async.impl.ioc-macros/return-chan state_20202 inst_20200)))]
                  (if (clojure.core/identical? result__6712__auto__ :recur) (recur) result__6712__auto__)))
              (catch
               java.lang.Throwable
               ex__6713__auto__
                (clojure.core.async.impl.ioc-macros/aset-all! state_20202 2 ex__6713__auto__)
                (if
                 (clojure.core/seq (clojure.core.async.impl.ioc-macros/aget-object state_20202 4))
                  (clojure.core.async.impl.ioc-macros/aset-all!
                   state_20202
                   1
                   (clojure.core/first (clojure.core.async.impl.ioc-macros/aget-object state_20202 4)))
                  (throw ex__6713__auto__))
                :recur)
              (finally
                (clojure.core.async.impl.ioc-macros/aset-object state_20202 3 (clojure.lang.Var/getThreadBindingFrame))
                (clojure.lang.Var/resetThreadBindingFrame old-frame__6710__auto__)))]
            (if (clojure.core/identical? ret-value__6711__auto__ :recur) (recur state_20202) ret-value__6711__auto__))))
       state__6967__auto__
       (clojure.core/->
        (f__6966__auto__)
        (clojure.core.async.impl.ioc-macros/aset-all!
         clojure.core.async.impl.ioc-macros/USER-START-IDX
         c__6964__auto__
         clojure.core.async.impl.ioc-macros/BINDINGS-IDX
         captured-bindings__6965__auto__))]
       (clojure.core.async.impl.ioc-macros/run-state-machine-wrapped state__6967__auto__))))
   c__6964__auto__)

  ;;
  )

(comment
  

  (macroexpand '(go (let []
                      (<! (chan 1)))))
  (let*
   [c__6964__auto__ (clojure.core.async/chan 1) captured-bindings__6965__auto__ (clojure.lang.Var/getThreadBindingFrame)]
   (clojure.core.async.impl.dispatch/run
    (fn*
     []
     (clojure.core/let
      [f__6966__auto__
       (clojure.core/fn
         state-machine__6709__auto__
         ([]
          (clojure.core.async.impl.ioc-macros/aset-all!
           (java.util.concurrent.atomic.AtomicReferenceArray. 6)
           0
           state-machine__6709__auto__
           1
           1))
         ([state_22214]
          (clojure.core/let
           [old-frame__6710__auto__
            (clojure.lang.Var/getThreadBindingFrame)
            ret-value__6711__auto__
            (try
              (clojure.lang.Var/resetThreadBindingFrame (clojure.core.async.impl.ioc-macros/aget-object state_22214 3))
              (clojure.core/loop
               []
                (clojure.core/let
                 [result__6712__auto__
                  (clojure.core/case
                   (clojure.core/int (clojure.core.async.impl.ioc-macros/aget-object state_22214 1))
                    1
                    (clojure.core/let
                     [inst_22210 (chan 1)]
                      (clojure.core.async.impl.ioc-macros/take! state_22214 2 inst_22210))
                    2
                    (clojure.core/let
                     [inst_22212 (clojure.core.async.impl.ioc-macros/aget-object state_22214 2)]
                      (clojure.core.async.impl.ioc-macros/return-chan state_22214 inst_22212)))]
                  (if (clojure.core/identical? result__6712__auto__ :recur) (recur) result__6712__auto__)))
              (catch
               java.lang.Throwable
               ex__6713__auto__
                (clojure.core.async.impl.ioc-macros/aset-all! state_22214 2 ex__6713__auto__)
                (if
                 (clojure.core/seq (clojure.core.async.impl.ioc-macros/aget-object state_22214 4))
                  (clojure.core.async.impl.ioc-macros/aset-all!
                   state_22214
                   1
                   (clojure.core/first (clojure.core.async.impl.ioc-macros/aget-object state_22214 4)))
                  (throw ex__6713__auto__))
                :recur)
              (finally
                (clojure.core.async.impl.ioc-macros/aset-object state_22214 3 (clojure.lang.Var/getThreadBindingFrame))
                (clojure.lang.Var/resetThreadBindingFrame old-frame__6710__auto__)))]
            (if (clojure.core/identical? ret-value__6711__auto__ :recur) (recur state_22214) ret-value__6711__auto__))))
       state__6967__auto__
       (clojure.core/->
        (f__6966__auto__)
        (clojure.core.async.impl.ioc-macros/aset-all!
         clojure.core.async.impl.ioc-macros/USER-START-IDX
         c__6964__auto__
         clojure.core.async.impl.ioc-macros/BINDINGS-IDX
         captured-bindings__6965__auto__))]
       (clojure.core.async.impl.ioc-macros/run-state-machine-wrapped state__6967__auto__))))
   c__6964__auto__)

  ;;
  )


(comment

  (macroexpand '(go (let []
                      (prn (opr :main/ops| ::mount1))
                      (do (chan 1)))))

  (let*
   [c__6964__auto__ (clojure.core.async/chan 1) captured-bindings__6965__auto__ (clojure.lang.Var/getThreadBindingFrame)]
   (clojure.core.async.impl.dispatch/run
    (fn*
     []
     (clojure.core/let
      [f__6966__auto__
       (clojure.core/fn
         state-machine__6709__auto__
         ([]
          (clojure.core.async.impl.ioc-macros/aset-all!
           (java.util.concurrent.atomic.AtomicReferenceArray. 6)
           0
           state-machine__6709__auto__
           1
           1))
         ([state_22234]
          (clojure.core/let
           [old-frame__6710__auto__
            (clojure.lang.Var/getThreadBindingFrame)
            ret-value__6711__auto__
            (try
              (clojure.lang.Var/resetThreadBindingFrame (clojure.core.async.impl.ioc-macros/aget-object state_22234 3))
              (clojure.core/loop
               []
                (clojure.core/let
                 [result__6712__auto__
                  (clojure.core/case
                   (clojure.core/int (clojure.core.async.impl.ioc-macros/aget-object state_22234 1))
                    1
                    (clojure.core/let
                     [inst_22232 (do (prn (opr :main/ops| :pad.go1/mount1)) (do (chan 1)))]
                      (clojure.core.async.impl.ioc-macros/return-chan state_22234 inst_22232)))]
                  (if (clojure.core/identical? result__6712__auto__ :recur) (recur) result__6712__auto__)))
              (catch
               java.lang.Throwable
               ex__6713__auto__
                (clojure.core.async.impl.ioc-macros/aset-all! state_22234 2 ex__6713__auto__)
                (if
                 (clojure.core/seq (clojure.core.async.impl.ioc-macros/aget-object state_22234 4))
                  (clojure.core.async.impl.ioc-macros/aset-all!
                   state_22234
                   1
                   (clojure.core/first (clojure.core.async.impl.ioc-macros/aget-object state_22234 4)))
                  (throw ex__6713__auto__))
                :recur)
              (finally
                (clojure.core.async.impl.ioc-macros/aset-object state_22234 3 (clojure.lang.Var/getThreadBindingFrame))
                (clojure.lang.Var/resetThreadBindingFrame old-frame__6710__auto__)))]
            (if (clojure.core/identical? ret-value__6711__auto__ :recur) (recur state_22234) ret-value__6711__auto__))))
       state__6967__auto__
       (clojure.core/->
        (f__6966__auto__)
        (clojure.core.async.impl.ioc-macros/aset-all!
         clojure.core.async.impl.ioc-macros/USER-START-IDX
         c__6964__auto__
         clojure.core.async.impl.ioc-macros/BINDINGS-IDX
         captured-bindings__6965__auto__))]
       (clojure.core.async.impl.ioc-macros/run-state-machine-wrapped state__6967__auto__))))
   c__6964__auto__)
  ;;
  )


