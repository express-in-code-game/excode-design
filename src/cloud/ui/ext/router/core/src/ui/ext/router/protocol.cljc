(ns ui.ext.router.protocol)

(defprotocol Router
  (abc [_])
  (ghi [_]))

(defprotocol Vals
  (op [_ v])
  (op-history-pushed [_])
  (vl-history-pushed [_ data]))