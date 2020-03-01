(ns app.alpha.part
  (:require [clojure.repl :refer [doc]]))

(comment

  (def part {:app.part/key :app.part.key/user-data
             :app.part/state {:some :state}
             :app.part/depends-on [:app.part.key/another-part]
             :app.part/mount (fn [ctx part a b]
                               (part/get-state ctx :app.part.key/another-part)
                               (part/update-state part {:some :other-state}))
             :app.part/unmount (fn [ctx part a b])
             :app.part/status (fn [ctx part])
             :app.part/update-state (fn [ctx part])
             :app.part/get-state (fn [ctx part])})


  (def ctx
    {:parts {:user-data-app {:part 'app.alpha.streams.users/part
                             :depends-on [:another-app]}
             :another-app {:depends-on []}}
     :state {:some-val 3
             :another-val (fn [ctx]
                            :some-logic)}})

  (ns-unmap *ns* 'get-state)
  ;;
  )

(comment

  (derive java.lang.Object ::object)
  (derive java.util.Map ::map)
  (derive java.util.Collection ::coll)
  (derive (class :a-keyword) ::key)

  (defmulti get-state (fn [ctx part] [(class ctx) (class part)]))
  (defmethod get-state [::map ::map] [ctx part] :map-map)
  (defmethod get-state [::map ::key] [ctx part] :map-key)
  (defmethod get-state [::object ::object] [ctx part] :objects)
  (defmethod get-state [::object java.lang.Comparable] [ctx part] :object-comparable)
  (prefer-method get-state [::object java.lang.Comparable] [::object ::object])
  (prefer-method get-state  [::map ::map] [::object ::object])
  #_(defmethod get-state :default [ctx part] :oops)

  ;;
  )




