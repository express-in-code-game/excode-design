(ns starnet.pad.protocols.bar1)

(defprotocol Bar
  (-one [_ x])
  (-two [_ x] [_ x y])
  (-bar [_]))