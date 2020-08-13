(ns mult.impl.channels
  (:require
   [goog.string :refer [format]]
   [clojure.string :as string]
   [cljs.reader :refer [read-string]]
   [mult.protocols :as p]))

(def ^:const TOPIC :topic)
(def ^:const OP :op)

(defn main|i
  []
  (let []
    (reify
      p/Op
      (-op [_ v] (get v OP))
      p/Vals
      (-op-init [_] :main/init)
      (-vl-init [_] {OP (p/-op-init _)})
      (-op-activate [_] :main/activate)
      (-vl-activate [_ editor-context] {OP (p/-op-activate _) :editor-context editor-context})
      (-op-deactivate [_] :main/deactivate)
      (-vl-deactivate [_] {OP (p/-op-deactivate _)})
      (-op-start-proc [_] :main/start-proc)
      (-vl-start-proc [_ proc-fn] {OP (p/-op-start-proc _) :proc-fn proc-fn})
      (-op-stop-proc [_] :main/stop-proc)
      (-vl-stop-proc [_ proc-id] {OP (p/-op-stop-proc _) :proc-id proc-id})
      (-op-restart-proc [_] :main/restart-proc)
      (-vl-restart-proc [_ proc-id] {OP (p/-op-restart-proc _) :proc-id proc-id})
      (-op-proc-started [_] :main/proc-started)
      (-vl-proc-started [_ proc-id proc|] {OP (p/-op-proc-started _) :proc-id proc-id :proc| proc|})
      (-op-proc-stopped [_] :main/proc-stopped)
      (-vl-proc-stopped [_ proc-id] {OP (p/-op-proc-stopped _) :proc-id proc-id}))))

(defn log|i
  []
  (let []
    (reify
      p/Op
      (-op [_ v] (get v OP))
      p/Vals
      (-op-exinfo [_] :exinfo)
      (-vl-exinfo [_ ex] {OP (p/-op-exinfo _) :ex ex})
      (-op-log [_] :log)
      (-vl-log [_ comment] {OP (p/-op-log _) :comment comment})
      (-vl-log [_ comment data] {OP (p/-op-log _) :comment comment :data data})
      (-vl-log [_ id  comment data] {OP (p/-op-log _) :id id :comment comment :data data}))))

(defn ops|i
  []
  (let []
    (reify
      p/Op
      (-op [_ v] (get v OP))
      p/Vals
      (-op-activate [_] :ops/activate)
      (-vl-activate [_] {OP (p/-op-activate _)})
      (-op-deactivate [_] :ops/deactivate)
      (-vl-deactivate [_] {OP (p/-op-deactivate _)})
      (-op-connect [_] :ops/connect)
      (-vl-connect [_ opts]  {OP (p/-op-connect _) :opts opts})
      (-op-disconnect [_] :ops/disconnect)
      (-vl-disconnect [_ opts] {OP (p/-op-disconnect _) :opts opts})
      (-op-tab-disposed [_] :ops/tab-disposed)
      (-vl-tab-disposed [_ id] {OP (p/-op-tab-disposed _) :tab/id id})
      (-op-texteditor-changed [_])
      (-vl-texteditor-changed [_ data] {OP (p/-op-texteditor-changed _) :data  data}))))

(defn cmd|i
  []
  (let []
    (reify
      p/Op
      (-op [_ v] (get v OP))
      p/Vals
      (-op-cmd [_] :cmd/cmd)
      (-vl-cmd [_ id args] {OP (p/-op-cmd _) :cmd/id id}))))

(defn editor|i
  []
  (let []
    (reify
      p/Op
      (-op [_ v] (get v OP)))))

(defn tab|i
  []
  (let []
    (reify
      p/Op
      (-op [_ v] (get v OP))
      p/Vals
      (-op-tab-append [_] :tab/append)
      (-vl-tab-append [_ data] {OP (p/-op-tab-append _) :data data})
      (-op-conf [_] :tab/conf)
      (-vl-conf [_ conf] {OP (p/-op-conf _) :conf conf})
      (-op-namespace-changed [_])
      (-vl-namespace-changed [_ data] {OP (p/-op-namespace-changed _) :data data}))))

(defn netsock|i
  []
  (let []
    (reify
      p/Op
      (-op [_ v] (get v OP))
      p/Vals
      (-op-connected [_] :netsock/connected)
      (-vl-connected [_ opts] {OP (p/-op-connected _) :opts opts})
      (-op-ready [_] :netsock/ready)
      (-vl-ready [_ opts] {OP (p/-op-ready _) :opts opts})
      (-op-timeout [_] :netsock/timeout)
      (-vl-timeout [_ opts] {OP (p/-op-timeout _) :opts opts})
      (-op-disconnected [_] :netsock/disconnected)
      (-vl-disconnected [_ hadError opts] {OP (p/-op-disconnected _) :opts opts :hadError hadError})
      (-op-error [_] :netsock/error)
      (-vl-error [_ err opts] {OP (p/-op-error _) :opts opts :err err})
      (-op-data [_] :netsock/data)
      (-vl-data [_ data opts] {OP (p/-op-data _) :data data :opts opts}))))