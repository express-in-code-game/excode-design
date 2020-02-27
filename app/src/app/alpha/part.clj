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

  (derive java.util.Map ::map)
  (derive java.util.Collection ::coll)
  (derive (class :a-keyword) ::key)
  (isa? (class :asd) ::key)

  (isa? (class (seq [1])) java.util.Collection)
  (isa? (class {}) java.util.Collection)
  (isa? 3 3)


  (defmulti get-state class)
  (defmethod get-state ::map [x] :a-map)
  (defmethod get-state java.util.Collection [x] 'java.util.Collection)
  (defmethod get-state :default [x] :oops)

  (get-state {})
  (get-state [])

  (ns-unmap *ns* 'get-state)
  ;;
  )

(derive java.util.Map ::map)
(derive java.util.Collection ::coll)
(derive (class :a-keyword) ::key)

(defmulti get-state (fn [ctx part] (class part)))
(defmethod get-state ::map [ctx part] :a-map )
(defmethod get-state ::key [ctx part] :a-key)
#_(defmethod get-state :default [ctx part] :oops)

(comment 
  
  
  (get-state {} {})
  (get-state {} :a-keyword)
  (get-state {} "as")
  
  ;;
  )



