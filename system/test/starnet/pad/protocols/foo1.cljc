(ns starnet.pad.protocols.foo1)

(defprotocol Foo
  (-one [_ x])
  (-two [_ x] [_ x y])
  (-foo [_]))