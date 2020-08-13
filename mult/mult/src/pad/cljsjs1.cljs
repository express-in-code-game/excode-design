(ns pad.cljsjs1
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]

   [goog.string :refer [format]]
   [clojure.string :as string]
   [clojure.pprint :refer [pprint]]
   [cljs.reader :refer [read-string]]
   [goog.dom :as gdom]
   [goog.events :as events]
   [goog.object :as gobj]
   [cljs.js :as cljs]
   [cljs.analyzer :as ana]
   [cljs.tools.reader :as r]
   [cljs.tools.reader.reader-types :refer [string-push-back-reader]]
   [cljs.tagged-literals :as tags]))

(def st (cljs/empty-state))

#_(let [eval cljs.core/*eval*]
    (set! cljs.core/*eval*
          (fn [form]
            (binding [cljs.env/*compiler* st
                      *ns* (find-ns 'mult.extension #_cljs.analyzer/*cljs-ns*)
                      cljs.js/*eval-fn* cljs.js/js-eval]
              (prn "(.-name *ns*) " (.-name *ns*))
              (eval form)))))

(def form1 '(let [x (fn [] 3)
                  o (x)]
              (prn o)
              o))

(def form2 '(fn [file-uri] (cljs.core/re-matches #".+\.cljs" file-uri)))

(defn test1
  []
  [(eval '(cljs.core.async/chan 10))
   (let [f (eval form2)]
     (f "abc.cljs"))
   (eval '(re-matches #".+clj" "abc.clj"))
   (apply (eval '(fn abc [a b c] #{a b c})) [1 2 3])
   (eval '(mult.impl.editor/js-lookup (clj->js {:a 1 :b 2}) ))])

(comment

  (eval '[(cljs.core/fn [] 3)])

  ;;
  )

(comment

  
  
  
  (def f (eval '(fn [file-uri]
                  (re-match #".+.cljs" file-uri))))

  (type (eval '[]))
  (eval '(cljs.core/+ 1 1))
  
  (defonce state (env/default-compiler-env))
  (cljs.js/eval-str state
                    "(ns user) 3"
                    ""
                    {:eval cljs.js/js-eval}
                    prn)

  (cljs.js/js-eval {:source "1 + 1"})

  (cljs.js/eval-str (cljs.js/empty-state)
                    "(ns my.user) (map inc [1 2 3])"
                    ""
                    {:eval cljs/js-eval}
                    prn)

  cljs.core/*eval*

  (def state (cljs.js/empty-state))

  
  (eval '(cljs.core/+ 1 1))

  (eval '(cljs.core/re-matches #".+.clj" "/code/starnet/alpha/main.clj"))
  (eval '(cljs.core/re-find #".+.clj" "/code/starnet/alpha/main.clj"))

  (eval 'mult.impl.editor/editor)

  (def state (cljs.js/empty-state))

  (defn evaluate [source cb]
    (cljs.js/eval-str state source nil {:eval cljs.js/js-eval :context :expr} cb))


  (def f (eval '(fn [file-uri]
                  (cljs.core/re-matches #".+.cljs" file-uri))))

  (eval (list inc 10))
  
  (eval '(let [x 3]
           x))
  
  (eval '(do  ))
  ;;
  )


(comment


  

;; -----------------------------------------------------------------------------
;; Example 0

  (def ex0-src
    (str "(defn foo [a b]\n"
         "  (interleave (repeat a) (repeat b)))\n"
         "\n"
         "(take 5 (foo :read :eval))"))

  (def out (atom {}))
  
  (cljs/eval-str st ex0-src 'ex0.core
                 {:eval cljs/js-eval
                  :source-map true}
                 (fn [{:keys [error value]}]
                   (if-not error
                     (reset! out value)
                     (do
                       (reset! out error)
                       (.error js/console error)))))



  ;;
  )