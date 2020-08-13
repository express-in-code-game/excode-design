(ns pad.protocols.channels)

(defprotocol Op
  (-op [_ v] "Returns the operation name (a keyword) of the value"))

(defprotocol Log|
  (-op-step [_])
  (-op-info [_])
  (-op-warning [_])
  (-op-error [_])
  (-step [_ id comment data])
  (-info [_ id comment data])
  (-warning [_ id comment data])
  (-error [_ id comment data])
  (-explain [_ id result comment data]))

(defprotocol System|
  (-proc-started [_ id v])
  (-proc-stopped [_ id v])
  (-procs-up [_ ])
  (-procs-down [_]))