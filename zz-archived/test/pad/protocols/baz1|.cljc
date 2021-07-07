(ns starnet.pad.protocols.baz1|)


(defprotocol Baz1|
  :extend-via-metadata true
  (-one [_ x])
  (-two [_ x] [_ x y])
  (-baz [_]))