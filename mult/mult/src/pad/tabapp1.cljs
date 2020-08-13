(ns pad.tabapp1
  (:require
   [goog.string :refer [format]]
   [cljs.reader :refer [read-string]]
   [clojure.pprint :refer [pprint]]

   [goog.dom :as gdom]
   [goog.events :as events]
   [goog.object :as gobj]
   [cljs.js :as cljs]
   [cljs.analyzer :as ana]
   [cljs.tools.reader :as rdr]
   [cljs.tools.reader.reader-types :refer [string-push-back-reader]]
   [cljs.tagged-literals :as tags]))


(defn ^:export main
  []
  (pprint "main"))


(comment


  (def st (cljs/empty-state))

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


  (let [eval cljs.core/*eval*
        st (cljs.js/empty-state)]
    (set! cljs.core/*eval*
          (fn [form]
            (binding [cljs.env/*compiler* st
                      *ns* (find-ns cljs.analyzer/*cljs-ns*)
                      cljs.js/*eval-fn* cljs.js/js-eval]
              (prn form)
              (prn eval)
              (eval form)))))
  (eval '(cljs.core/+ 1 1))
  (eval '(do (defn x [] 3) (x)))


  ;;
  )