(ns starnet.pad.protocols.protocols1
  (:require
   [clojure.core.async :as a :refer [<! >! <!! chan go alt! take! put! offer! poll! alts! to-chan
                                     timeout thread pub sub  >!! <!! alt!! alts!! close! mult tap untap
                                     sliding-buffer dropping-buffer
                                     go-loop pipeline pipeline-async pipeline-blocking]]
   [starnet.pad.protocols.foo1 :as foo1]
   [starnet.pad.protocols.bar1 :as bar1]))

(comment

  (def o (reify
           foo1/Foo
           (foo1/-one [_ x] [x])))

  (foo1/-one o 1)
  (satisfies? foo1/Foo o) ; => true
  (extends? foo1/Foo (type o)) ; => true

  (def o (reify
           foo1/Foo
           (foo1/-one [_ x] [x])
           bar1/Bar
           (bar1/-one [_ x] #{x})))
  ; => Duplicate method name "_one" with signature ...

  (def o (reify
           foo1/Foo
           (foo1/-one [_ x] [x])
           bar1/Bar
           (bar1/-two [_ x] #{x})))

  (bar1/-two o 1)

  ;https://groups.google.com/forum/#!topic/clojure/pdfj13ppwik
  ;https://ask.clojure.org/index.php/1952/cannot-implement-protocol-methods-of-the-same-name-inline?show=2307

  ;https://clojure.atlassian.net/browse/CLJ-1625

  (def o (with-meta {} {`foo1/-one (fn [_ x] [x])
                        `foo1/-two (fn [_ x y] [x y])
                        `foo1/-foo (fn [_] [])
                        `bar1/-one (fn [_ x] #{x})}))

  (bar1/-one o 1)
  (foo1/-one o 1)
  (foo1/-foo o)
  (satisfies? foo1/Foo o) ; => false
  (extends? foo1/Foo (type o)) ; => false
  
  ;;
  )


(comment

  ; collison

  (defprotocol A
    (abc [_ x]))

  (defprotocol B
    (b1 [_ x] [_ x y])
    (b2 [_ x])
    (b22 [_ x y]))

  ; https://clojure.org/reference/protocols#_extend_via_metadata

  (defprotocol Component
    :extend-via-metadata true
    (start [component]))

  (def component (with-meta {:name "db"} {`start (constantly "started")}))
  (start component)

  (def b (reify
           B
           (b1 [_ x] [x])
           (b1 [_ x y] [x y])
           (b2 [_ x] [x])))

  (b1 b 1)
  (b2 b 1)



  ;;
  )