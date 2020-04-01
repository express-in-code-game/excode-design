(ns starnet.alpha.common.game.render
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
   [starnet.alpha.common.core :refer [make-inst with-gen-fmap]]
   [clojure.test :as test :refer [is are run-all-tests testing deftest run-tests]]

   [reagent.core :as r]
   [goog.string :refer [format]]

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

(defn make-store
  ([]
   (make-store {}))
  ([{:keys [channels] :as opts}]
   (let [state (make-state opts)
         state* (r/atom state)
         map* (r/atom {:m/status :initial
                       :m/entities [1]})
         entities* (r/cursor map* [:m/entities])
         count-entities* (r/track! (fn []
                                     (let [xs @entities*]
                                       (when xs
                                         (count xs)))))]
     (merge
      state
      {:ra.g/state state*
       :ra.g/map map*
       :ra.g/entities entities*
       :ra.g/count-entities count-entities*
       :db/ds nil
       :channels channels}))))