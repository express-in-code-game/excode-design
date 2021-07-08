;; (ns github.sergeiudris.deathstar-lab.scenarios.rovers.game-api
;;   (:require
;;    [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
;;                                      pub sub unsub mult tap untap mix admix unmix pipe
;;                                      timeout to-chan  sliding-buffer dropping-buffer
;;                                      pipeline pipeline-async]]

;;    [github.sergeiudris.deathstar-lab.scenarios.rovers.core :as rovers.core]
;;    [github.sergeiudris.deathstar-lab.scenarios.rovers.spec :as rovers.spec]))


;; (def ^:private state (rovers.core/create-state (rovers.core/generate-state-data)))

;; (add-watch state ::watcher
;;            (fn [key atom old-state new-state]
;;              (let [pos (:pos new-state)]
;;                (println (format "new position is %s" pos)))))

;; (def ^:private channels (rovers.core/create-channels))

;; (def ^:private input| (::rovers.core/input| channels))

;; (def ^:private scenario (rovers.core/create-proc-scenario channels {:state state}))

;; (def ^:private simulation (rovers.core/create-proc-simulation channels {:state state}))

;; (defn move
;;   [x y]
;;   (put! input| {:op ::rovers.core/move :pos {:x x :y y}}))

;; (defn scan
;;   []
;;   (put! input| {:op ::rovers.core/scan}))