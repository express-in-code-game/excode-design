(ns starnet.common.alpha.game.render
  (:require
   [clojure.repl :refer [doc]]
   [clojure.core.async :as a :refer [<! >!  timeout chan alt! go
                                     alts!  take! put! mult tap untap
                                     pub sub sliding-buffer mix admix unmix]]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [starnet.common.alpha.core :refer [make-inst with-gen-fmap]]
   [clojure.test :as test :refer [is are run-all-tests testing deftest run-tests]]

   [reagent.core :as r]

   ["antd/lib/layout" :default AntLayout]
   ["antd/lib/menu" :default AntMenu]
   ["antd/lib/icon" :default AntIcon]
   ["antd/lib/button" :default AntButton]
   ["antd/lib/list" :default AntList]
   ["antd/lib/row" :default AntRow]
   ["antd/lib/col" :default AntCol]
   ["antd/lib/form" :default AntForm]
   ["antd/lib/input" :default AntInput]
   ["antd/lib/popover" :default AntPopover]
   ["react" :as React]
   ["antd/lib/checkbox" :default AntCheckbox]


   ["antd/lib/divider" :default AntDivider]
   ["@ant-design/icons/SmileOutlined" :default AntSmileOutlined]))


(def ant-row (r/adapt-react-class AntRow))
(def ant-col (r/adapt-react-class AntCol))
(def ant-divider (r/adapt-react-class AntDivider))

(def ant-layout (r/adapt-react-class AntLayout))
(def ant-layout-content (r/adapt-react-class (.-Content AntLayout)))
(def ant-layout-header (r/adapt-react-class (.-Header AntLayout)))
(def ant-smile-outlined (r/adapt-react-class AntSmileOutlined))
(def ant-menu (r/adapt-react-class AntMenu))
(def ant-menu-item (r/adapt-react-class (.-Item AntMenu)))
(def ant-icon (r/adapt-react-class AntIcon))
(def ant-button (r/adapt-react-class AntButton))
(def ant-list (r/adapt-react-class AntList))
(def ant-input (r/adapt-react-class AntInput))
(def ant-input-password (r/adapt-react-class (.-Password AntInput)))
(def ant-checkbox (r/adapt-react-class AntCheckbox))
(def ant-popover (r/adapt-react-class AntPopover))
(def ant-form (r/adapt-react-class AntForm))
(def ant-form-item (r/adapt-react-class (.-Item AntForm)))

(def ra-test (r/atom {:status :initial
                      :entities []}))

#_[ant-list {:size "small"
             :bordered true
             :dataSource ["1" "2" "3"]
             :render-item (fn [x]
                            (r/as-element [:div  x]))}]

(defn testing!
  []
  (do
    (def -channels @(resolve 'starnet.common.alpha.game.store/-channels))
    (go
      (swap! ra-test assoc :status :starting)
      (let [c (chan 1)]
        (>! (-channels :ch-worker) {:worker/op :starnet.common.alpha.game.data/make-entities
                                    :worker/args [{}]
                                    :ch/c-out c})
        (swap! ra-test assoc :status :generating)
        (let [o (<! c)]
          (swap! ra-test merge {:status [:complete (count o)]
                                :entities o}))))))

(defn rc-raw-dom-grid
  [channels ratoms]
  (let [{:keys [ch-inputs]} channels
        ra-test-status* (r/cursor ra-test [:status])
        ra-test-entites* (r/cursor ra-test [:entities])
        count-entities* (ratoms :ra.g/count-entities)]
    (testing!)
    (fn [_ _]
      (let [count-entities @count-entities*
            entities @ra-test-entites*
            ra-test-status @ra-test-status*]
        [:<>
         [:section {:style {:width (str (* 64 16) "px")
                            :height (str (* 64 16) "px")
                            :display "flex"
                            :flex-wrap "wrap"}}
          (map (fn [x]
                 [ant-popover
                  {:placement "top"
                   :key (:e/uuid x)
                   :title "entity"
                   :trigger "click"
                   :content (r/as-element [:div
                                           [:p (str x)]])}
                  [:div {:class ["tile"]}]]) entities)]]))))

(defn rc-game
  [channels ratoms]
  (let [{:keys [ch-inputs]} channels
        uuid* (r/cursor (ratoms :ra.g/state) [:g/uuid])
        status* (r/cursor (ratoms :ra.g/state) [:g/status])
        m-status* (r/cursor (ratoms :ra.g/map) [:m/status])
        count-entities* (ratoms :ra.g/count-entities)
        entities* (ratoms :ra.g/entities)
        timer* (r/atom 0)
        _ (go (loop [c-interval (timeout 1000)
                     c-duration (timeout 2000000)]
                (alt!
                  c-interval (do
                               (swap! timer* inc)
                               (recur (timeout 1000)  c-duration))
                  c-duration (println "timer complete"))))]
    (fn [_ _]
      (let [uuid @uuid*
            status @status*
            m-status  @m-status* #_(-> @(ratoms :ra.g/map) :m/status)
            entities @entities*
            count-entities @count-entities*
            timer @timer*]
        [:<>
         [:div {:style {:position "absolute" :top 0 :left 0}}
          [:div  uuid]
          [:div  [:span "game status: "] [:span status]]
          [:div  [:span "map status: "] [:span (str m-status)]]
          [:div  [:span "count entities: "] [:span count-entities]]
          [:div  [:span "timer: "] [:span timer]]]]))))

(comment

  (go
    (swap! ra-test assoc :status :starting)
    ;;  (<! (timeout 3000))
    ;; (println "hello" (-> @ra-test :status))
    (make-entities {})
    (swap! ra-test assoc :status :generating)
    (make-entities {})
    ;; (<! (timeout 3000))
    (swap! ra-test assoc :status :complete))

 



  ;;
  )