(ns app.clojure.multi
  (:require [clojure.repl :refer [doc]]))


(comment
  ; https://clojure.org/reference/multimethods

  (isa? String Object)
  (isa? (class {}) java.util.Map)
  (isa? (class []) java.util.Collection)

  (derive java.util.Map ::collection)
  (derive java.util.Collection ::collection)
  (isa? java.util.HashMap ::collection)

  (defmulti foo class)
  (defmethod foo ::collection [c] :a-collection)
  (defmethod foo String [s] :a-string)
  (foo [])
  ; :a-collection
  (foo (java.util.HashMap.))
  ; :a-collection
  (foo "bar")
  ; :a-string



  (defmulti area :Shape)
  (defn rect [wd ht] {:Shape :Rect :wd wd :ht ht})
  (defn circle [radius] {:Shape :Circle :radius radius})
  (defmethod area :Rect [r]
    (* (:wd r) (:ht r)))
  (defmethod area :Circle [c]
    (* (. Math PI) (* (:radius c) (:radius c))))
  (defmethod area :default [x] :oops)
  (def r (rect 4 13))
  (def c (circle 12))
  (area r)
  (area c)
  (area {})

  ;;
  )

