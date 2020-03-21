(ns starnet.ui.csp.main
  (:require
   [clojure.repl :refer [doc]]
   [reagent.core :as r]
   [clojure.core.async :as a :refer [<! >!  timeout chan alt! go
                                     alts!  take! put!
                                     pub sub sliding-buffer mix admix unmix]]
   [goog.string :as gstring]
   [goog.string.format]
   [clojure.spec.test.alpha :as stest]
   [clojure.spec.alpha :as s]

   [starnet.common.alpha.spec]

   [starnet.ui.alpha.spec]
   [starnet.ui.alpha.repl]
   [starnet.ui.alpha.tests]))

(declare proc-main proc-render-containers)

(def channels (let [ch-proc-main (chan 1)
                    ch-sys (chan (sliding-buffer 10))
                    pb-sys (pub ch-sys :ch/topic (fn [_] (sliding-buffer 10)))]
                {:ch-proc-main ch-proc-main
                 :ch-sys ch-sys
                 :pb-sys pb-sys}))

(defn ^:export main
  []
  (put! (channels :ch-proc-main) {:proc/op :start})
  (proc-main (select-keys channels [:ch-proc-main :ch-sys])))


(defn proc-main
  [{:keys [ch-proc-main ch-sys]}]
  (go (loop []
        (when-let [{op :proc/op} (<! ch-proc-main)]
          (println (gstring/format "proc-main %s" op))
          (condp = op
            :start (do
                     (proc-render-containers (select-keys channels [:pb-sys :ch-sys]))
                     (put! (channels :ch-sys) {:ch/topic :proc-render-containers :proc/op :mount}))))
        (recur))
      (println "closing go block: proc-main")))

(defn proc-render-containers
  [{:keys [pb-sys]}]
  (let [c (chan 1)
        root-el (.getElementById js/document "ui")]
    (sub pb-sys :proc-render-containers c)
    (go (loop []
          (when-let [{:keys [proc/op]} (<! c)]
            (println (gstring/format "proc-render-containers %s" op))
            (condp = op
              :mount (do (r/render [:<>
                                    [:div {:id "div-1"}]
                                    [:div {:id "div-2"}]
                                    [:div {:id "div-3"}]] root-el))
              :unmount (r/render nil root-el)))
          (recur))
        (println "proc-render-containers closing"))
    c))



(comment

  (put! (channels :ch-sys) {:ch/topic :proc-render-containers :proc/op :unmount})

  ;;
  )