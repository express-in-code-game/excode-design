(ns pad.protocols.procs)

(defprotocol Procs
  (-start [_ proc-id])
  (-stop [_ proc-id])
  (-restart [_ proc-id])
  (-up [_ ctx])
  (-down [_])
  (-downup [_ ctx])
  (-up? [_]))