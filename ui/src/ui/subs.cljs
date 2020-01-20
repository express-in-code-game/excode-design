(ns ui.subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 ::active-view
 (fn [db _]
   (:ui.db.core/active-view db)))

(rf/reg-sub
 ::module-count
 (fn [db _]
   (:ui.db.core/module-count db)))
