(ns deathstar.extension.chan
  #?(:cljs (:require-macros [deathstar.extension.chan]))
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.spec.alpha :as s]
   [cljctools.csp.op.spec :as op.spec]
   [deathstar.extension.spec :as extension.spec]))

(do (clojure.spec.alpha/check-asserts true))

(defmulti ^{:private true} op* op.spec/op-spec-dispatch-fn)
(s/def ::op (s/multi-spec op* op.spec/op-spec-retag-fn))
(defmulti op op.spec/op-dispatch-fn)

(defn create-channels
  []
  (let [ops| (chan 10)
        ops|m (mult ops|)
        ops|x (mix ops|)]
    {::ops| ops|
     ::ops|m ops|m
     ::ops|x ops|x}))

(defmethod op*
  {::op.spec/op-key ::update-settings-filepaths
   ::op.spec/op-type ::op.spec/request} [_]
  (s/keys :req [::op.spec/out|]

(defmethod op
  {::op.spec/op-key ::update-settings-filepaths
   ::op.spec/op-type ::op.spec/request}
  ([op-meta channels]
   (op op-meta channels (chan 1)))
  ([op-meta channels out|]
   (put! (::ops| channels)
         (merge op-meta
                {::op.spec/out| out|}))
   out|))


(defmethod op*
  {::op.spec/op-key ::update-settings-filepaths
   ::op.spec/op-type :response} [_]
  (s/keys :req [::extension.spec/settings-filepaths]))

(defmethod op
  {::op.spec/op-key ::update-settings-filepaths
   ::op.spec/op-type ::op.spec/response}
  [op-meta out| settings-filepaths]
  (put! out|
        (merge op-meta
               {::extension.spec/settings-filepaths settings-filepaths})))

(defmethod op*
  {::op.spec/op-key ::apply-settings-file
   ::op.spec/op-type ::op.spec/request} [_]
  (s/keys :req [::extension.spec/filepath ::op.spec/out|]))

(defmethod op
  {::op.spec/op-key ::apply-settings-file
   ::op.spec/op-type ::op.spec/request}
  ([op-meta channels filepath]
   (op op-meta channels filepath (chan 1)))
  ([op-meta channels filepath out|]
   (put! (::ops| channels) (merge op-meta
                                  {::extension.spec/filepath filepath
                                   ::op.spec/out| out|}))
   out|))

(defmethod op*
  {::op.spec/op-key ::apply-settings-file
   ::op.spec/op-type ::op.spec/response} [_]
  (s/keys :req []
          :opt []))

(defmethod op
  {::op.spec/op-key ::apply-settings-file
   ::op.spec/op-type ::op.spec/response}
  [op-meta out|]
  (put! out| op-meta))

(comment

  (def ^:const meta-keys [:op :op-type])

  (defmulti ^:private op*
    (fn [value] (select-keys value meta-keys)))

  (defmethod op*
    {:op ::update-settings-filepaths
     :op-type :request}
    [_]
    (s/keys :req [::op.spec/out|]))

  (defmethod op*
    {:op ::update-settings-filepaths
     :op-type :response}
    [_]
    (s/keys :req [::extension.spec/settings-filepaths]))

; https://clojuredocs.org/clojure.spec.alpha/multi-spec
; generated-value will be {:op ::something ::extension.spec/some-data 123}
; dispatch-tag will be :request or :resonse or :something or {:op ::some-op :op-type :request} ?
; dispatch-tag returns the dispatching value of a multimehtod picked during generation? 

  (s/def ::op (s/multi-spec op* (fn [generated-value dispatch-tag])))

  ;https://clojure.org/reference/multimethods
  ; Quote: Note that the first test of isa? is =, so exact matches work.
  ; (isa? {::a 1} {::a 1}) => true

  (condp = (select-keys value [:op :op-type])

    {:op ::update-settings-filepaths
     :op-type :request}
    (do nil)

    {:op :update-settings-filepaths
     :op-type :response}
    (do nil))


  (op {:op ::update-settings-filepaths
       :op-type :request}
      channels opts)

  (defmulti op
    (fn [value-meta & args] (select-keys value-meta meta-keys)))

  (defmethod op
    {:op ::apply-settings-file
     :op-type :request}
    ([vmeta channels filepath]
     (op vmeta channels filepath (chan 1)))
    ([vmeta channels filepath out|]
     (put! (::ops| channels) (merge vmeta
                                    {::filepath filepath
                                     :out| out|}))
     out|))

;;
  )