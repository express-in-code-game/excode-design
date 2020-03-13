(ns starnet.ui.csp.main
  (:require
   [clojure.repl :refer [doc]]
   [reagent.core :as r]
   [clojure.core.async :as a :refer [<! >! timeout chan alt! go
                                     alts!  take! put!
                                     pub sub]]

   [clojure.spec.test.alpha :as stest]
   [clojure.spec.alpha :as s]

   [starnet.common.alpha.spec]

   [starnet.ui.alpha.spec]
   [starnet.ui.alpha.repl]
   [starnet.ui.alpha.tests]))


(defn ^:export main
  []
  (println "csp"))

(def chan-1 (chan (a/sliding-buffer 10)))
(def chan-1-pub (pub chan-1 first))
(def view-1 (atom {}))

(defn proc-main
  [p out]
  (let [c (chan 1)]
    (sub p :main c)
    (go (loop []
          (when-let [[[t v] c] (alts! [c])]
            (condp = v
              :init (do (println "initilized"))))
          (recur))
        (println "closing go block: proc-main"))
    c))

(defn proc-render-containers
  [p out]
  (let [c (chan 1)]
    (sub p :main c)
    (go (loop []
          (when-let [[[t v] c] (alts! [c])]
            (condp = v
              :init (do (r/render [:<>
                                   [:div {:id "div-1"}]
                                   [:div {:id "div-2"}]
                                   [:div {:id "div-3"}]]
                                  (.getElementById js/document "ui"))
                        (println "rendered containers")
                        )))
          (recur))
        (println "closing go block: proc-render-containers"))
    c))

(proc-main chan-1-pub chan-1)
(proc-render-containers chan-1-pub chan-1)

(comment

  (put! chan-1 [:main :init])

  ;;
  )