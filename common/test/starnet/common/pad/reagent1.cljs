(ns starnet.common.pad.reagent1
  (:require
   [clojure.repl :refer [doc]]
   [reagent.core :as r]
   [reagent.dom :as rdom]
   [clojure.core.async :as a :refer [<! >!  timeout chan alt! go
                                     alts!  take! put! mult tap untap
                                     pub sub sliding-buffer mix admix unmix]]
   [goog.string :as gstring]
   [goog.string.format]

   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check :as tc]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop])
  (:import [goog.net XhrIo EventType WebSocket]
           [goog Uri]
           goog.history.Html5History))

(comment

  (defonce click-count (r/atom 0))


  (defn state-ful-with-atom []
    (fn []
      (println "rendering state-ful-with-atom")
      [:div {:on-click #(swap! click-count inc)}
       "I have been clicked " @click-count " times."]))

  (defn ordinary-component []
    (fn []
      (println "rendering ordinary-component")
      (js/console.log (r/current-component))
      [state-ful-with-atom]))



  (rdom/render [layout
                [:<>
                 [ordinary-component]]]  (.getElementById js/document "ui"))

  (swap! click-count inc)

  (def x1 (r/atom 1))

  (defn f1 []
    (js/console.log @x1)
    (+ @x1 2))
  (def t1 (r/track f1))
  (def t2 (r/track! f1))
  (defn f2 []
    (+ @t2 2))
  (def t3 (r/track! f2))
  @x1
  @t1
  @t2
  @t3
  (swap! x1 inc)

  (add-watch x1 :f1 (fn [k ref old nw]
                      (println nw)))

  (defn state-ful-with-atom-2 []
    (fn []
      (println "rendering state-ful-with-atom-2")
      [:div {:on-click #(swap! x1 inc)}
       "I have been clicked " @t3 " times."]))

  (rdom/render [:<>
                [state-ful-with-atom-2]]  (.getElementById js/document "ui"))


  '{:a 1
    :b 2
    :c (derived-state (fn [ctx old-val c-out]
                        (let [a @(ctx :a)
                              b @(ctx :b)]
                          (go
                            '...
                            (put! c-out (or new-val old-val))))))}

  (def a (r/atom []))
  (add-watch a :f1 (fn [k ref old nw]
                     (println nw)))
  (def x1 [1])
  (def x2 [1])
  (swap! a  (constantly x1))


  (take! (timeout 1000) (fn [_] (println "x")))


  ;;
  )