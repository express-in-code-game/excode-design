(ns starnet.pad.protocols.bar1)

(defprotocol Bar
  :extend-via-metadata true
  (-one [_ x])
  (-two [_ x] [_ x y])
  (-bar [_]))