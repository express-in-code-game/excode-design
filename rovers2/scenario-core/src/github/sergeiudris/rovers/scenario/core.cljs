(ns github.sergeiudris.rovers.scenario.core
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [clojure.spec.test.alpha :as stest]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]

   ["@flatten-js/core"
    :default flattenjs
    :rename {BooleanOperations flattenjs.boolean
             Relations flattenjs.relations}]

   [github.sergeiudris.rovers.scenario.spec :as scenario.spec]))


(comment
  
  ;;
  )

(defn gen-entities
  [x y]
  (let [#_rovers #_(gen/generate scenario.spec/rovers-gen 30 42)
        entities-gen (gen/vector scenario.spec/entity-gen (* x y))
        entities-vec (gen/generate entities-gen 30 42)
        entities (for [ix (range 0 x)
                       iy (range 0 y)
                       :let [entity (get entities-vec (+ ix (* iy x)))]
                       :when (not= (::scenario.spec/entity-type entity) ::scenario.spec/sands)]
                   (do
                     (merge
                      entity
                      {::scenario.spec/x ix
                       ::scenario.spec/y iy})))
        entities-map (reduce (fn [result entity]
                               (assoc result (::scenario.spec/id entity) entity)) {} entities)]
    entities-map
    #_(reduce (fn [result rover]
                (let [entity (rand-nth entities)]
                  (-> result
                      (dissoc (::scenario.spec/id entity))
                      (assoc (::scenario.spec/id rover)
                             (merge rover
                                    (select-keys entity [::scenario.spec/x ::scenario.spec/y]))))))
              entities-map  rovers)))

(defn distance
  [entity1 entity2]
  (let [point1 (.point flattenjs (::scenario.spec/x entity1)  (::scenario.spec/y entity1))
        point2 (.point flattenjs (::scenario.spec/x entity2)  (::scenario.spec/y entity2))]
    (first (.distanceTo point1 point2))))

(defn rover-closest-location
  [state rover opts]
  (let [{:keys [::scenario.spec/entities-in-rovers-range-per-rover
                ::scenario.spec/visited-locations]} state
        {:keys [::scenario.spec/location-type]} opts]
    (->> (get entities-in-rovers-range-per-rover (::scenario.spec/id rover))
         (filter (fn [[k location]] (and
                                     (not=
                                      (select-keys rover [::scenario.spec/x ::scenario.spec/y])
                                      (select-keys location [::scenario.spec/x ::scenario.spec/y]))
                                     (not (get visited-locations k)))))
         (group-by (fn [[k location]] (::scenario.spec/entity-type location)))
         (reduce (fn [result [entity-type locations]]
                   (->> locations
                        (sort-by (fn [[k location]]
                                   (distance rover location)))
                        (assoc result entity-type))) {})
         (keep (fn [[entity-type locations]]
                 (when (second (first locations))
                   [entity-type (second (first locations))])))
         (sort-by #(= (first %) location-type))
         (reverse)
         (first)
         (second))))


(defn add-location-to-visited
  [state rover location]
  (let []
    (assoc-in state [::scenario.spec/visited-locations (::scenario.spec/id location)]  location)))

(defn rover-visits-location
  [state rover location]
  (cond
    
    (and location)
    (let [distance (distance rover location)
          {:keys [::scenario.spec/energy-level]} rover
          energy-level-next-raw (+
                                 energy-level
                                 (::scenario.spec/energy location)
                                 (- (* distance 10)))
          energy-level-next  (max
                              0
                              (if (> energy-level-next-raw 100)
                                100
                                energy-level-next-raw))]
      (cond
        (= 0 energy-level)
        (let []
          (println "No energy")
          state)

        :else
        (-> state
            (update-in [::scenario.spec/rovers (::scenario.spec/id rover)]
                       merge (select-keys location [::scenario.spec/x
                                                    ::scenario.spec/y])
                       {::scenario.spec/energy-level energy-level-next})
            (add-location-to-visited rover location))))

    :else state))

(defn rover-scans
  [state rover opts]
  (let [{:keys [::scenario.spec/energy-percentage]} opts
        {:keys [::scenario.spec/energy-level
                ::scenario.spec/rover-vision-range]} rover
        energy-spent (* energy-level energy-percentage)
        energy-level-next-raw (+
                               energy-level
                               (- energy-spent))
        energy-level-next  (max
                            0
                            (if (> energy-level-next-raw 100)
                              100
                              energy-level-next-raw))
        rover-vision-range-next (+ rover-vision-range (/ energy-spent 10))]
    (cond
      (= 0 energy-level)
      (let []
        (println "No energy")
        state)

      :else
      (-> state
          (update-in [::scenario.spec/rovers (::scenario.spec/id rover)]
                     merge
                     {::scenario.spec/energy-level energy-level-next
                      ::scenario.spec/rover-vision-range rover-vision-range-next})))))

(defn entities-to-groups
  [entities]
  (->>
   entities
   (reduce
    (fn [result [k entity]]
      (cond

        (= (::scenario.spec/entity-type entity) ::scenario.spec/rover)
        (assoc-in result [::scenario.spec/rovers k] entity)

        :else
        (assoc-in result [::scenario.spec/locations k] entity)))
    {})))

(defn entities-in-range
  [state]
  (let [{:keys [::scenario.spec/rovers
                ::scenario.spec/entities]} state
        
        make-range-geometry
        (fn [rover]
          (.circle
           flattenjs
           (.point flattenjs (::scenario.spec/x rover) (::scenario.spec/y rover))
           (::scenario.spec/rover-vision-range rover)))

        make-entity-geometry
        (fn [entity]
          (.point flattenjs (::scenario.spec/x entity) (::scenario.spec/y entity)))

        geometries-intersect?
        (fn [range-geometry entity-geometry]
          (not (empty?
                (.intersect
                 range-geometry
                 entity-geometry))))

        partial-state
        (->>
         (for [[k-rover rover] rovers
               :let [range-geometry (make-range-geometry rover)]
               [k-entity entity] entities
               :let [entity-geometry (make-entity-geometry entity)]
               :when (geometries-intersect? range-geometry entity-geometry)]
           [[k-rover rover] [k-entity entity]])
         (reduce
          (fn [result [[k-rover rover] [k-entity entity]]]
            (-> result
                (assoc-in [::scenario.spec/entities-in-rovers-range k-entity] entity)
                (assoc-in [::scenario.spec/entities-in-rovers-range-per-rover k-rover k-entity] entity)))
          {}))]
    partial-state))


#_(defn create-state
    [opts]
    (let [state* (reagent.core/atom {})
          entities* (reagent.core/cursor state* [::entities])
          rovers* (reagent.core/cursor state* [::rovers])
          locations* (reagent.core/cursor state* [::locations])
          entities-in-rovers-range* (reagent.core/cursor state* [::entities-in-rovers-range])
          entities-in-rovers-range-per-rover* (reagent.core/cursor state* [::entities-in-rovers-range-per-rover])
          visited-locations* (reagent.core/cursor state* [::visited-locations])
          hovered-entity* (reagent.core/cursor state* [::hovered-entity])
          state  {::state* state*
                  ::entities* entities*
                  ::rovers* rovers*
                  ::locations* locations*
                  ::entities-in-rovers-range* entities-in-rovers-range*
                  ::entities-in-rovers-range-per-rover* entities-in-rovers-range-per-rover*
                  ::visited-locations* visited-locations*
                  ::hovered-entity* hovered-entity*}]
      (do
        (reagent.ratom/run-in-reaction
         (fn [] @entities*)
         state*
         ::derive-entity-groups
         (fn [_]
           (let [entities (::entities @state*)
                 partial-state
                 (->>
                  entities
                  (reduce
                   (fn [result [k entity]]
                     (cond

                       (= (::entity-type entity) ::rover)
                       (assoc-in result [::rovers k] entity)

                       :else
                       (assoc-in result [::locations k] entity)))
                   {})
                  (into {}))]
             (println ::derive-entity-groups)
             (println (count entities))
             (swap! state* merge partial-state)))
         {:no-cache false #_true})

        (reagent.ratom/run-in-reaction
         (fn [] @rovers*)
         state*
         ::entities-in-rovers-range
         (fn [_]
           )
         {:no-cache false #_true}))
      state))





#_(defn create-watchers
    [state]
    (let [rover* (reagent.core/cursor state [::rover])
          entities* (reagent.core/cursor state [::entities])
          trackf-entities-in-range (fn []
                                     (let [rover @rover*
                                           entities @entities*]
                                       (when (and rover entities)
                                         (let [entities-in-range (filter-entities-in-range entities rover)]
                                           #_(println (count entities-in-range))
                                           (swap! state assoc ::entities-in-range entities-in-range)))
                                       #_(println (count entities))
                                       #_(println (select-keys [::x ::y] rover))))
          tracked-entities-in-range (reagent.core/track! trackf-entities-in-range)]
      #_(add-watch state ::watch-state
                   (fn [key atom-ref old-state new-state]
                     (when (and
                            (::entities new-state) (::rover new-state)
                            (or
                             (not (identical? (::entities old-state) (::entities new-state)))
                             (not (identical? (::rover old-state) (::rover new-state)))))
                       (let [entites (::entities new-state)
                             rover (::rover new-state)
                             entities-in-range (filter-entities-in-range entites rover)]
                         (swap! state assoc ::entities-in-range entities-in-range)))))
      #_(add-watch rover* ::watch-rover
                   (fn [key atom-ref old-state new-state]
                     (println ::watch-rover)))))
