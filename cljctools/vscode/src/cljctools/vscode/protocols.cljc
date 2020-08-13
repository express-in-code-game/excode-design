(ns cljctools.vscode.protocols)

(defprotocol Send
  (-send [_ v]))

(defprotocol Release
  :extend-via-metadata true
  (-release [_]))

(defprotocol Active
  (-active? [_]))

(defprotocol Editor
  (-selection [_])
  (-register-commands [_ commands])
  (-create-tab [_ tabid])
  (-read-workspace-file [_ filepath])
  (-show-info-msg [_ msg])
  (-active-ns [_] "nil if it's not clj file")
  (-join-workspace-path [_ subpath]))

