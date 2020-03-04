(ns common.alpha.macros)

(defmacro defmethods-for-a-set
  "Iterates over a #{} of :ev/type keywords and calls defmethod"
  [mmethod kwset]
  `(doseq [kw# ~kwset]
     (defmethod ~mmethod kw# [x#] kw#)))