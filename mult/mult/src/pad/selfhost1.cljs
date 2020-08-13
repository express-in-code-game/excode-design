(ns pad.selfhost1
  (:require [cljs.js :as cljs]
            [cljs.env :as env]
            [shadow.cljs.bootstrap.node :as boot]
            ["fs" :as fs]
            ["path" :as path]))

(defn print-result [{:keys [error value] :as result}]
  (js/console.log "result" result)
  #_(set! (.-innerHTML (js/document.getElementById "dump")) value))

(def code
  "
(ns simpleexample.core
  (:require [clojure.string :as str]
            [reagent.core :as r]))
(defonce timer (r/atom (js/Date.)))
(defonce time-color (r/atom \"#f34\"))
(defonce time-updater (js/setInterval
                       #(do
   (reset! timer (js/Date.))
   (prn @timer)
   ) 1000))

")

(defonce compile-state-ref (env/default-compiler-env))

(let [eval cljs.core/*eval*]
  (set! cljs.core/*eval*
        (fn [form]
          (binding [cljs.env/*compiler* compile-state-ref
                    *ns* (find-ns 'mult.extension #_cljs.analyzer/*cljs-ns*)
                    cljs.js/*eval-fn* cljs.js/js-eval]
            (prn "(.-name *ns*) " (.-name *ns*))
            (eval form)))))

(def result (atom nil))

(defn compile-it [code ns]
  (cljs/eval-str
   compile-state-ref
   code
   "[test]"
   {:eval cljs/js-eval
    :ns ns
    :load (partial boot/load compile-state-ref)}
   #(reset! result %)))


(defn start [editor-context]
  (let [extpath (. editor-context -extensionPath)
        path (.join path extpath "resources/out/bootstrap")]
    (boot/init compile-state-ref
               {:path path}
               (fn []
                 (prn "initialized")
                 (let [eval cljs.core/*eval*]
                   (set! cljs.core/*eval*
                         (fn [form]
                           (binding [cljs.env/*compiler* compile-state-ref
                                     *ns* (find-ns 'mult.extension #_cljs.analyzer/*cljs-ns*)
                                     cljs.js/*eval-fn* cljs.js/js-eval]
                             (prn "(.-name *ns*) " (.-name *ns*))
                             (eval form)))))
                 #_(compile-it code 'cljs.user)))))

(defn stop [])

(comment
  (.readFileSync fs)

  (start (-> @mult.extension/proc-main-state :ctx :editor-context))

  (compile-it "(prn 3 4)" 'pad.selfhost1)
  (compile-it "(prn proc-main) (prn 123)" 'mult.extension)

  (compile-it "(in-ns (find-ns 'mult.extention)) (prn 3)")

  (def f (eval '(fn [x] [x])))
  (eval '(let [x 3]
           x))
  (eval '(let [x (fn [] 3)]
           x))
  (eval '(fn []))

  (f 1)

  (eval '(do 3))

  (def form2 '(fn [file-uri] (cljs.core/re-matches #".+\.cljs" file-uri)))
  (eval '(re-matches #".+clj" "abc.clj"))
  (eval '(chan 10))
  (compile-it "(prn (chan 10))" (find-ns 'mult.extension))
  (require '[clojure.core.async :as a])
  a/chan
  
  (eval '(cljs.core.async/chan 10))
  (def a cljs.core.async)
  (a/chan 10)
  
  (eval '(let [a cljs.core.async]
           [(a/chan 10) a/mult]))



  ;;
  )

; https://github.com/mhuebert/shadow-bootstrap-example/blob/master/src/shadow_eval/core.cljs

(defonce c-state (cljs/empty-state))

(def source-examples ["(circle 40)"
                      "(for [n (range 10)] n)"
                      "(defcell x 10)"
                      "(cell (interval 100 inc))"
                      "(require '[cljs.js :as cljs])\n\n(fn? cljs/eval-str)"])

(defn eval-str [source cb]
  (cljs/eval-str
   c-state
   source
   "[test]"
   {:eval cljs/js-eval
    :load (partial boot/load c-state)
    :ns   (symbol "shadow-eval.user")}
   cb))

