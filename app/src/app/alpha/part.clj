(ns app.alpha.part
  (:require [clojure.repl :refer [doc]]))


(comment

  (def part {:app.part/key :app.part.key/user-data
             :app.part/state {:some :state}
             :app.part/depends-on [:app.part.key/another-part]
             :app.part/mount (fn [ctx part a b]
                               (part/get-state :app.part.key/another-part)
                               (part/update-state part {:some :other-state}))
             :app.part/unmount (fn [ctx part a b])
             :app.part/status (fn [ctx part])
             :app.part/update-state (fn [ctx part])
             :app.part/get-state (fn [ctx part])})

  (derive java.util.Map ::map)

  (defmulti get-state class)
  (defmethod get-state ::map [x] :a-map)
  (defmethod get-state :default [x] :oops)

  (get-state {})

  ;;
  )



