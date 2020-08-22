(ns deathstar.multiplayer.protocols)

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



(defprotocol Scenario
  (-foo [_]))

(defprotocol Simulation
  (-bar [_]))