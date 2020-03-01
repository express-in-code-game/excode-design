(ns app.clojure.multi
  (:require [clojure.repl :refer [doc]]
            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]))


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
  (ancestors java.util.Collection)


  (defmulti get-state class)
  (defmethod get-state ::map [x] :a-map)
  (defmethod get-state java.util.Collection [x] 'java.util.Collection)
  (defmethod get-state :default [x] :oops)

  (get-state {})
  (get-state [])

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
  (defmethod get-state :default [ctx part] :oops)

  (get-state {} {})
  (get-state {} :a-keyword)
  (get-state {} "as")

  (ancestors (class []))
  (ancestors java.lang.String)
  (isa? java.lang.String ::object)
  
  ; on arity
  ; https://stackoverflow.com/questions/10313657/is-it-possible-to-overload-clojure-multi-methods-on-arity

  ;;
  )


(comment

  (defmulti variadic (fn [& args] (mapv class args)))
  (defmethod variadic [String] [& args] [:string])
  (defmethod variadic [String String]  [& args] [:string :string])
  (defmethod variadic [String java.util.Map] [& args] [:string :map])
  (defmethod variadic [Number java.util.Map]  [& args] [:number :map])
  (ancestors (class {}))
  (ns-unmap *ns* 'variadic)

  (variadic "asd")
  (variadic "asd" "asd")
  (variadic "asd" {})
  (isa? (class {}) java.util.Map)
  (variadic 1 {})


  (ns-unmap *ns* 'send-event)
  (defmulti send-event
    "send event to kafka"
    {:arglists '([] [topic] [topic a-num] [a-num topic])}
    (fn [& args] [(count args) (mapv class args)]))
  (defmethod send-event [0 []] [& args] [])
  (defmethod send-event [1  [Number]]  [& args] [:number])
  (defmethod send-event [1  [String]]  [& args] [:string])
  (defmethod send-event [2  [String Number]] [& args] [:string :number])
  (defmethod send-event [2  [Number String]] [& args]  [:number :string])
  (defmethod send-event [2  [Number Number]] [& args]  [:number :number])
  (defmethod send-event [2  [java.util.Map Number]] [& args] [:map :number])
  (defmethod send-event [3  [java.util.Map Number String]] [& args] [:map :number :string])

  (send-event)
  (send-event 1)
  (send-event "asd")
  (send-event "asd" 1)
  (send-event  1 "asd")
  (send-event  1 1)
  (send-event  {} 1)
  (send-event  {} 1 "asd")
  
  (send-event :a)
  (send-event :a :b)

  (s/fdef send-event
    :args (s/alt :0 (s/cat)
                 :number (s/cat :a number?)
                 :string (s/cat :a string?)
                 :string-number (s/cat :a string? :b number?)
                 :number-string (s/cat :a number? :b string?)
                 :number-number (s/cat :a number? :b number?)
                 :map-number (s/cat :a map? :b number?)
                 :map-number-string (s/cat :a map? :b number? :c string?)))

  (stest/instrument `send-event)
  (stest/unstrument `send-event)

  ;;
  )