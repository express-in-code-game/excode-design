(ns pad.protocols.proc)

(defprotocol Proc
  (-start [_] [_ out|])
  (-stop [_] [_ out|])
  (-running? [_]))
