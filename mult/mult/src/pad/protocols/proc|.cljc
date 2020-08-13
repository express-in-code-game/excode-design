(ns pad.protocols.proc|)

(defprotocol Proc|
  (-op-start [_])
  (-op-stop [_])
  (-op-started [_])
  (-op-stopped [_])

  (-start [_ out|])
  (-stop [_ out|])
  (-started [_])
  (-stopped [_]))