
(ns deathstar.extension.chan-api
  #?(:cljs (:require-macros [deathstar.extension.chan-api]))
  (:require
   [clojure.spec.alpha :as s]
   [clojure.test.check.generators :as gen]

   [deathstar.multiplayer.spec :as multiplayer.spec]
   [deathstar.multiplayer.remote.spec :as remote.spec]))

(do (clojure.spec.alpha/check-asserts true))

(s/def ::update-gui-state (s/keys ::req []))

(defn create-channels-http-chan
  []
  (let [request| (chan 10)
        request|m (mult request|)]
    {::request| request|
     ::request|m request|m}))

(defn create-channels
  []
  (let [extension-ops| (chan 10)
        extension-ops|m (mult extension-ops|)
        extension-ops|x (mix extension-ops|)
        cmd| (chan 10)
        cmd|m (mult cmd|)
        tab-state| (chan (sliding-buffer 10))
        tab-state|m (mult tab-state|)
        input| (chan 10)]
    {::extension.spec/cmd| cmd|
     ::extension.spec/cmd|m cmd|m
     ::extension.spec/tab-state| tab-state|
     ::extension.spec/tab-state|m tab-state|m
     ::extension.spec/input| input|

     ::core.spec/extension-ops| extension-ops|
     ::core.spec/extension-ops|m extension-ops|m}))

(comment


  (def ^:const OP :op)
  (s/def ::out| any?)

  (def op-specs
    {::update-gui-state (s/keys ::req-un [::op])})

  (def ch-specs
    {::ops| #{}
     ::input| #{} ; inputs come from gui are same as cmd| - same set of operations
     })

  (def op-keys (set (keys op-specs)))
  (def ch-keys (set (keys ch-specs)))

  (s/def ::op op-keys)

  (s/def ::ch-exists ch-keys)
  (s/def ::op-exists (fn [v] (op-keys (if (keyword? v) v (OP v)))))
  (s/def ::ch-op-exists (s/cat :ch ::ch-exists :op ::op-exists))

  (defmacro op
    [chkey opkey]
    (s/assert ::ch-exists  chkey)
    (s/assert ::op-exists  opkey)
    `~opkey)

  (defmacro vl
    [chkey v]
    (s/assert ::ch-exists  chkey)
    `~v)

  (def cmd-ids #{"deathstar.open"
                 "deathstar.ping"
                 "deathstar.gui.open"
                 "deathstar.open-resource-space-tab"
                 "deathstar.solution-tab-eval"})

  (s/def ::cmd-ids cmd-ids)

  (defmacro cmd-id
    [id]
    (s/assert ::cmd-ids id)
    `~id)

  #_(defmacro assert-op
      [chkey opkey]
      `(do
         (s/assert ::ch-exists  ~chkey)
         (s/assert ::op-exists  ~opkey)
         #_(when-not (opkeys ~opkey)
             (throw (Exception. "no such op")))))

;; (defmulti op-type OP)

;; (defmethod op-type :app.main/mount
;;   [vl]
;;   (s/keys :req [::op ::out|]))

;; (s/def ::op (s/multi-spec op-type OP))
;; 

  #_(s/def ::ch-op-exists (fn [{:keys [channel-key val-map op-key]}]
                            ((channel-key channels) (or op-key (OP val-map)))))
  #_(s/def ::val-map-valid (fn [{:keys [channel-key val-map]}]
                             (s/valid? ((OP val-map) ops) val-map)))

  #_(s/valid? (:project.app.main/mount ops) {:op :project.app.main/mount})
  #_(gen/generate (s/gen (:project.app.main/mount ops)))

  #_(defmacro op
      [channel-key op-key]
      `~op-key)

  #_(s/fdef op
      :args (s/and
             (s/cat :channel-key ::channel-exists
                    :op-key ::op-exists)
             ::channel-op-exists)
      :ret any?)

  #_(defmacro vl
      [channel-key val-map]
      `~val-map)

  #_(s/fdef vl
      :args (s/and
             (s/cat :channel-key ::channel-exists
                    :val-map ::op-exists)
             ::channel-op-exists
             ::val-map-valid)
      :ret any?)

  ;;
  )