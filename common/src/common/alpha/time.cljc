(ns common.alpha.time
  #?(:clj
     (:require
      [clojure.repl :refer [doc]])
     :cljs
     (:require
      [clojure.repl :refer [doc]]
      [goog.date :as gdate]))
  #?(:clj (:import
           java.util.Date)))
  
  
(defn mk-inst
  "Returns a damn inst"
  []
  #?(:clj (java.util.Date.)
     :cljs (js/Date.)))

