(ns mult.protocols)

(defprotocol Op
  (-op [_ v] "Returns the operation name (a keyword) of the value"))

(defprotocol Vals
  (-op-init [_])
  (-vl-init [_])
  (-op-activate [_])
  (-vl-activate [_] [_ ctx])
  (-op-deactivate [_])
  (-vl-deactivate [_])

  (-op-log [_])
  (-vl-log [_  comment] [_ comment data]  [_ id comment data])
  (-op-exinfo [_])
  (-vl-exinfo [_  ex])

  (-op-cmd [_])
  (-vl-cmd [_ id args])

  (-op-start-proc [_])
  (-vl-start-proc [_ proc-fn])
  (-op-stop-proc [_])
  (-vl-stop-proc [_ proc-id])
  (-op-restart-proc [_])
  (-vl-restart-proc [_ proc-id])
  (-op-proc-started [_])
  (-vl-proc-started [_ proc-id proc|])
  (-op-proc-stopped [_])
  (-vl-proc-stopped [_ proc-id])

  (-op-tab-append [_])
  (-vl-tab-append [_ data])
  (-op-tab-disposed [_])
  (-vl-tab-disposed [_ id])
  (-op-conf [_])
  (-vl-conf [_ conf])
  (-op-texteditor-changed [_])
  (-vl-texteditor-changed [_ data])
  (-op-namespace-changed [_])
  (-vl-namespace-changed [_ data])

  (-op-connect [_])
  (-vl-connect [_ opts])
  (-op-disconnect [_])
  (-vl-disconnect [_ opts])

  (-op-connected [_])
  (-vl-connected [_ opts])
  (-op-ready [_])
  (-vl-ready [_ opts])
  (-op-timeout [_])
  (-vl-timeout [_ opts])
  (-op-disconnected [_])
  (-vl-disconnected [_ hadError opts])
  (-op-error [_])
  (-vl-error [_ err opts])
  (-op-data [_])
  (-vl-data [_] [_ data opts]))

(defprotocol Connect
  (-connect [_])
  (-disconnect [_])
  (-connected? [_]))

(defprotocol Send
  (-send [_ v]))

(defprotocol Eval
  (-eval [_ opts] [_ code ns-sym] [_ conn code ns-sym] [_ conn code ns-sym session]))

(defprotocol Release
  :extend-via-metadata true
  (-release [_]))

(defprotocol Active
  (-active? [_]))

(defprotocol ReplConn
  (-nrepl-op [_ opts])
  (-clone-session [_] [_ opts])
  (-close-session [_ session opts])
  (-describe [_  opts])
  #_(-eval [_ code session-id opts])
  (-interrupt [_ session opts])
  #_(-load-file [_ file opts])
  (-ls-sessions [_])
  #_(-sideloader-provide [_ content name session type opts])
  #_(-sideloader-start [_ session opts])
  #_(-stdin [_ stdin-content opts]))

(defprotocol Editor
  (-selection [_])
  (-register-commands [_ commands])
  (-create-tab [_ tabid])
  (-read-workspace-file [_ filepath])
  (-show-info-msg [_ msg])
  (-active-ns [_] "nil if it's not clj file")
  (-join-workspace-path [_ subpath]))

