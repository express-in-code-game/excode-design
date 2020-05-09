(ns app.main
  (:require
   [clojure.core.async :as a :refer [<! >! <!! timeout chan alt! go close!
                                     >!! <!! alt!! alts! alts!! take! put! mult tap untap
                                     thread pub sub sliding-buffer mix admix unmix]]
   [app.api]
   [app.api.http]
   [app.api.que])
  (:gen-class))

(def proc (app.api/create-proc))

(defn -main [& args]
  #_(Thread/sleep Long/MAX_VALUE)
  #_(set! app.api.http/*proc* proc)
  (app.api.http/start))