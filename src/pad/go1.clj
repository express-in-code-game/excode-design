(ns pad.go1
  (:require
   [clojure.core.async :as a :refer [<! >! <!! timeout chan alt! go close!
                                     >!! <!! alt!! alts! alts!! take! put! mult tap untap
                                     thread pub sub sliding-buffer mix admix unmix]]
   [clojure.spec.alpha :as s]
   [clojure.test.check.generators :as gen]))

(defprotocol Mountable
  :extend-via-metadata true
  (mount* [_] [_ opts])
  (unmount* [_] [_ opts]))

(def ^:const OP :op)

(s/def ::out| any?)

(def ops
  {:project.app.main/mount (s/keys :req-un [::op #_::out|])
   :project.app.main/unmount (s/keys :req-un [::op #_::out|])})

(def ops-keys (set (keys ops)))
(s/def ::op ops-keys)

(def channels
  {:main/ops| #{:project.app.main/mount :project.app.main/unmount}
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

#_(s/valid? (:project.app.main/mount ops) {:op :project.app.main/mount})
#_(gen/generate (s/gen (:project.app.main/mount ops)))

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
(defn f5-2-4
  [ops|]
  (go (loop []
        (opr :main/ops| ::mount1)
        (do ops|))))

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