(ns starnet.pad.protocols.foo1)

(defprotocol Foo
  :extend-via-metadata true
  (-one [_ x])
  (-two [_ x] [_ x y])
  (-foo [_]))