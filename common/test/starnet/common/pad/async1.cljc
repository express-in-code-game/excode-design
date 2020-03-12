(ns starnet.common.pad.async1
  (:require
   [clojure.core.async :as a :refer [<! >! <!! timeout chan alt! go
                                     >!! <!! alt!! alts! alts!! take! put!
                                     thread]]
   ))

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

(defn google-go [query]
  (let [c (chan)
        t (timeout 80)]
    (go (>! c (<! (fastest query web1 web2))))
    (go (>! c (<! (fastest query image1 image2))))
    (go (>! c (<! (fastest query video1 video2))))
    (go (loop [i 0 ret []]
          (if (= i 3)
            ret
            (recur (inc i) (conj ret (alt! [c t] ([v] v)))))))))

(defn google-async [query]
  (let [c (chan)
        t (timeout 80)]
    (future (>!! c (<!! (fastest query web1 web2))))
    (future (>!! c (<!! (fastest query image1 image2))))
    (future (>!! c (<!! (fastest query video1 video2))))
    (loop [i 0 ret []]
      (if (= i 3)
        ret
        (recur (inc i) (conj ret (alt!! [c t] ([v] v))))))))

(comment

  (<!! (google-go "clojure"))
  (google-async "clojure")
  
  

  ;;
  )


(defn fan-in [ins]
  (let [c (chan)]
    (go (while true
          (let [[x c0] (alts! ins)]
            (>! c x))))
    c))

(defn fan-out [in cs-or-n]
  (let [cs (if (number? cs-or-n)
             (repeatedly cs-or-n chan)
             cs-or-n)]
    (go (while true
          (let [x (<! in)
                outs (map #(vector % x) cs)]
            (alts! outs))))
    cs))



(comment

  (let [cout (chan)
        cin (fan-in (fan-out cout (repeatedly 3 chan)))]
    (go (dotimes [n 10]
          (>! cout n)
          (prn (<! cin))))
    nil)
  
  ;;
  )


(defn fan-in-2 [ins]
  (let [c (chan)]
    (future (while true
              (let [[x] (alts!! ins)]
                (>!! c x))))
    c))

(defn fan-out-2 [in cs-or-n]
  (let [cs (if (number? cs-or-n)
             (repeatedly cs-or-n chan)
             cs-or-n)]
    (future (while true
              (let [x (<!! in)
                    outs (map #(vector % x) cs)]
                (alts!! outs))))
    cs))

(comment

  (let [cout (chan)
        cin (fan-in-2 (fan-out-2 cout (repeatedly 3 chan)))]
    (dotimes [n 10]
      (>!! cout n)
      (prn (<!! cin))))
  
  ;;
  )


(comment

  ; https://www.youtube.com/watch?v=enwIIGzhahw
  ; https://github.com/halgari/clojure-conj-2013-core.async-examples

  (def c (chan))
  (take! c (fn [v] (println v)))
  (put! c "hi")
  (put! c 42 (fn [x] (println "done putting" x)))
  (take! c (fn [v] (println v)))

  (defn takep [c]
    (let [p (promise)]
      (take! c (fn [v] (deliver p v)))
      @p))

  (future (println "Got " (takep c)))

  (put! c 42)

  (defn putp [c val]
    (let [p (promise)]
      (put! c val (fn [_] (deliver p nil)))
      @p))

  (future (println "Done " (putp c :a)))
  (future (println "Got!" (takep c)))

  (future (println "Done " (>!! c 42)))
  (future (println "Got!" (<!! c)))

  (thread 42)
  (<!! (thread 42))
  (<!! (thread (println "It works!" (<!! (thread 42)))))

  (go 42)
  (<!! (go 42))
  (go (println "It works!" (<! (go 42))))

  (def fbc (chan 1))
  (go (>! fbc 1)
      (println "done 1"))
  (go (>! fbc 2)
      (println "done 2"))
  (<!! fbc)

  (def dbc (chan (a/dropping-buffer 1)))
  (go (>! dbc 1)
      (println "done 1"))
  (go (>! dbc 2)
      (println "done 2"))
  (<!! dbc)

  (def sbc (chan (a/sliding-buffer 1)))
  (go (>! sbc 1)
      (println "done 1"))
  (go (>! sbc 2)
      (println "done 2"))
  (<!! sbc)

  (def c (chan))
  (a/close! c)
  (<!! c)

  (def a (chan))
  (def b (chan))
  (put! a 42)
  (alts!! [a b])

  (<!! (timeout 1000))

  (alts!! [a (timeout 1000)])
  (alts!! [[a 42]
           (timeout 1000)])
  (alts!! [a]
          :default :nothing-found)

  (put! a :a)
  (put! b :b)
  (alts!! [a b])

  (put! a :a)
  (put! b :b)
  (alts!! [a b]
          :priority true)

  ; log 

  (def log-chan (chan))

  (thread
    (loop []
      (when-let [v (<!! log-chan)]
        #_(.println System/out v)
        (println v)
        (recur)))
    (println "log 1 closed"))

  (thread
    (loop []
      (when-let [v (<!! log-chan)]
        (.println System/out v)
        (recur)))
    (println "log 2 closed"))

  (a/close! log-chan)

  (defn log [msg]
    (>!! log-chan msg))

  (log "foo")

  ; mult

  (def to-mult (chan 1))
  (def m (a/mult to-mult))

  (let [c (chan 1)]
    (a/tap m c)
    (go (loop []
          (when-let [v (<! c)]
            (println "Got " v)
            (recur)))
        (println "exiting")))

  (>!! to-mult 42)

  (a/close! to-mult)

  ; pub sub

  


  ;;
  )



