


(comment

  (let [{:keys []} value]
    (println ::init)
    (try
      (let [options (clj->js {"accessController"
                              {"write" ["*"] #_[(.. orbitdb -identity -publicKey)]}})]
        (set! ipfs (IpfsClient "http://ipfs:5001"))
        (set! orbitdb (<p! (.createInstance OrbitDB ipfs (clj->js {"directory" "/root/.orbitdb"}))))
        (set! dblog (<p! (.log orbitdb TOPIC-ID options)))
        (<p! (.load dblog))
        (set! dbdocs (<p! (.docs orbitdb "foo")))
        (<p! (.load dbdocs))
        (do
          #_(println (.. orbitdb -identity -publicKey))
          #_(println (.-address dblog))
          (println (.toString (.-address dblog)))
          #_(println (.. dblog -identity -publicKey))
          #_(println (.. dbdocs -identity -publicKey)))
        (let [id (.-id (<p! (ipfs.id)))]
          (go (loop []
                (<! (timeout 2000))
                (.add dblog (pr-str {::app.spec/peer-id id
                                     ::locale-time-string (.toLocaleTimeString (js/Date.))
                                     ::random-int (rand-int 1000)}))
                (recur))))
        (.on (.-events dblog) "replicated" (fn [address]
                                             (-> dblog
                                                 (.iterator  #js {"limit" 1})
                                                 (.collect)
                                                 (.map (fn [e] (.-value (.-payload e))))
                                                 (println)))))
      (catch js/Error err (println err)))



    #_(<! (init-puppeteer))


    #_(go (loop []
            (<! (timeout 2000))
            #_(swap! state* update ::app.spec/counter inc)
            (ui.chan/op
             {::op.spec/op-key ::ui.chan/update-state
              ::op.spec/op-type ::op.spec/fire-and-forget}
             channels
             @state*)
            (recur)))
    #_(go
        (let [out| (chan 64)]
          (peernode.chan/op
           {::op.spec/op-key ::peernode.chan/request-pubsub-stream
            ::op.spec/op-type ::op.spec/request-stream
            ::op.spec/op-orient ::op.spec/request}
           channels
           out|)
          (loop []
            (when-let [value  (<! out|)]
              (println ::request-pubsub-stream)
              (println value)
              (recur)))))
    #_(go (loop []
            (<! (timeout (* 1000 (+ 1 (rand-int 2)))))
            (peernode.chan/op
             {::op.spec/op-key ::peernode.chan/pubsub-publish
              ::op.spec/op-type ::op.spec/fire-and-forget}
             channels
             {::some ::value})
            (recur))))


  ;;
  )

(comment

  (js/Object.keys ipfs)
  (js/Object.keys ipfs.pubsub)

  (go
    (let [id (<p! (daemon._ipfs.id))]
      (println (js-keys id))
      (println (.-id id))
      (println (format "id is %s" id))))


  (def orbitdb @(::app.spec/orbitdb* ctx))
  (go
    (def eventlog (<p! (.eventlog orbitdb "foo")))
    (<p! (.load eventlog)))
  (go
    (println (<p! (.add eventlog (pr-str {::app.spec/peer-id (::app.spec/peer-id @(::app.spec/state* ctx))
                                          ::random-int 1  #_(rand-int 1000)})))))
  (-> eventlog
      (.iterator  #js {"limit" -1})
      (.collect)
      (.map (fn [e]
              (println (js-keys e))
              (println (js-keys (.-payload e)))
              (println (.-hash e))
              (read-string (.-value (.-payload e)))))
      #_(first)
      (println))

  (go
    (<p! (.drop eventlog)))




  ;;
  )

(comment

  (try
    (let [ipfs (IpfsClient "http://ipfs:5001")]
      (reset! ipfs* ipfs)
      (swap! state* assoc ::app.spec/peer-id (.-id (<p! (.id ipfs))))
      (reset! orbitdb* (<p! (.createInstance OrbitDB ipfs (clj->js {"directory" "/root/.orbitdb"}))))
      (reset! tournaments-kvstore* (<p! (.keyvalue @orbitdb*
                                                   TOPIC-ID
                                                   (clj->js {"accessController"
                                                             {"write" ["*"]}})))))
    (let [tournaments-kvstore @tournaments-kvstore*]
      #_(<p! (.drop tournaments-kvstore))
      (<p! (.load tournaments-kvstore))
      (swap! state* assoc ::app.spec/tournaments
             (reduce
              (fn [result [k value]]
                (assoc result k (read-string value)))
              {}
              (js->clj (.-all tournaments-kvstore))))
      (println ::count-tournaments-kvstore (count (js->clj (.-all tournaments-kvstore))))
      (doseq [[k tournament] (get @state* ::app.spec/tournaments)]
        (app.chan/op
         {::op.spec/op-key ::app.chan/join-tournament
          ::op.spec/op-type ::op.spec/fire-and-forget}
         channels
         tournament))

      #_(println (keys (js->clj (.-all tournaments-kvstore))))
      (.on (.-events tournaments-kvstore)
           "replicated"
           (fn [address]
             (swap! state* assoc ::app.spec/tournaments
                    (reduce
                     (fn [result [k value]]
                       (assoc result k (read-string value)))
                     {}
                     (js->clj (.-all tournaments-kvstore))))
             #_(-> dblog
                   (.iterator  #js {"limit" 1})
                   (.collect)
                   (.map (fn [e] (.-value (.-payload e))))
                   (println)))))
    (catch js/Error err (println err)))


  ;;
  )