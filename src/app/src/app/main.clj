(ns app.main
  (:require
   [cljfx.api :as fx]))

(defn render []
  (fx/on-fx-thread
   (fx/create-component
    {:fx/type :stage
     :showing true
     :scene {:fx/type :scene
             :root {:fx/type :pagination
                    :page-count 10
                    :current-page-index 4
                    :page-factory (fn [i]
                                    {:fx/type :label
                                     :text (str "This is a page " (inc i))})}}})))

(defn -main [& args]
  (render))

