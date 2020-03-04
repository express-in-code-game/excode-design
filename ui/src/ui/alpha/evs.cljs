(ns ui.alpha.evs
  (:require
   [clojure.string :as string]
   [re-frame.core :as rf]
   [ui.db :refer [default-db]]
   #_[vimsical.re-frame.cofx.inject :as inject]
   [ajax.core :as ajax]
   [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
   [ui.core :refer  [deep-merge]]
   ))

(rf/reg-event-fx
 ::initialize-db
 (fn-traced [{:keys [db] :as coef} _]
            {:db ui.db/default-db}))

(rf/reg-event-db
 ::set-active-view
 (fn-traced [db [_ ea]]
            (assoc db :ui.db.core/active-view ea)))

(rf/reg-event-db
 ::set-re-pressed-example
 (fn-traced [db [_ ea]]
            (assoc db :re-pressed-example ea)))

(rf/reg-event-db
 ::inc-module-count
 (fn-traced [db [_ ea]]
            (let [kw :ui.db.core/module-count]
              (assoc db kw (inc (kw db))))))

(rf/reg-event-fx
 :xhrio-failure
 (fn [{:keys [db]} [_ ea]]
   (js/console.log ":xhrio-failure")
   (js/console.log ea)
   {}))

(rf/reg-event-db
 :http-no-on-failure
 (fn [db [_ ea]]
   (js/console.warn ":http-no-on-failure event "
                    ea)
   db))


