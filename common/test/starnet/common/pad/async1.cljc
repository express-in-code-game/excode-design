(ns starnet.common.pad.async1
  (:require
   [clojure.core.async :as a :refer [<! >! <!! timeout chan alt! go]]))

; https://github.com/clojure/core.async/wiki/Pub-Sub

(comment

  (def input-chan (a/chan))
  (def our-publication (a/pub input-chan :msg-type))
  (a/>!! input-chan {:msg-type :greeting :text "hello"})

  (def output-chan (a/chan))
  (a/sub our-publication :greeting output-chan)

  (def l (a/go-loop []
           (let [{:keys [text]} (a/<! output-chan)]
             (println (str "msg: " text))
             (recur))))

  (a/>!! input-chan {:msg-type :greeting :text "hello"})

  (let [c (a/chan)]
    (a/sub our-publication :greeting c)
    (a/go-loop []
      (let [{:keys [msg-type text]} (a/<! c)]
        (println (str "chan2 msg: " text))
        (recur))))

  (def loser-chan (a/chan))
  (a/sub our-publication :loser loser-chan)
  (a/>!! input-chan {:msg-type :loser :text "I won't be accepted"})

  (a/put! input-chan {:msg-type :greeting :text "hello2"})
  
  

  ;;
  )

; https://github.com/clojure/core.async/blob/master/examples/ex-go.clj

(defn fake-search [kind]
  (fn [c query]
    (go
      (<! (timeout (rand-int 100)))
      (>! c [kind query]))))

(def web1 (fake-search :web1))
(def web2 (fake-search :web2))
(def image1 (fake-search :image1))
(def image2 (fake-search :image2))
(def video1 (fake-search :video1))
(def video2 (fake-search :video2))

(defn fastest [query & replicas]
  (let [c (chan)]
    (doseq [replica replicas]
      (replica c query))
    c))

(defn google [query]
  (let [c (chan)
        t (timeout 80)]
    (go (>! c (<! (fastest query web1 web2))))
    (go (>! c (<! (fastest query image1 image2))))
    (go (>! c (<! (fastest query video1 video2))))
    (go (loop [i 0 ret []]
          (if (= i 3)
            ret
            (recur (inc i) (conj ret (alt! [c t] ([v] v)))))))))

(comment

  (<!! (google "clojure"))

  ;;
  )

