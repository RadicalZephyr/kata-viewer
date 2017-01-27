(ns kata-viewer.astar)

(defn neighbors
  ([edit-graph yx]
   (neighbors (concat [[0 -1] [-1 0]]
                      (when (edit-graph yx)
                        [[-1 -1]]))
              edit-graph
              yx))
  ([deltas edit-graph yx]
   (map #(mapv + yx %) deltas)))

(comment
 (defn neighbors
   ([size yx]
    (neighbors [[1 0] [0 1]] size yx))
   ([deltas size yx]
    (filter (fn [new-yx] (every? #(< -1 % size)
                                 new-yx))
            (map #(mapv + yx %) deltas))))

 (defn estimate-cost [step-cost-est sz y x]
   (* step-cost-est
      (- (+ sz sz) y x 2)))

 (defn path-cost [node-cost cheapest-nbr]
   (+ node-cost
      (or (:cost cheapest-nbr) 0)))

 (comment

   (path-cost 900 {:cost 1})
   ;;=> 901

   )

 (defn total-cost [newcost step-cost-est size y x]
   (+ newcost
      (estimate-cost step-cost-est size y x)))


 (defn astar [start-yx step-est cell-costs]
   (let [size (count cell-costs)]
     (loop [steps 0
            routes (vec (repeat size (vec (repeat size nil))))
            work-todo (sorted-set [0 start-yx])]
       (if (empty? work-todo)               ;; #: Check done
         [(peek (peek routes)) :steps steps] ;; #: Grab the first route
         (let [[_ yx :as work-item] (first work-todo) ;; #: Get next work item
               rest-work-todo (disj work-todo work-item) ;; #: Clear from todo
               nbr-yxs (neighbors size yx)      ;; #: Get neighbors
               cheapest-nbr (apply min-key :cost ;; #: Calc least-cost
                                   (keep #(get-in routes %)
                                         nbr-yxs))
               newcost (path-cost (get-in cell-costs yx) ;; #: Calc path so-far
                                  cheapest-nbr)
               oldcost (:cost (get-in routes yx))]
           (if (and oldcost (>= newcost oldcost)) ;; #: Check if new is worse
             (recur (inc steps) routes rest-work-todo)
             (recur (inc steps) ;; #: Place new path in the routes
                    (assoc-in routes yx
                              {:cost newcost
                               :yxs (conj (:yxs cheapest-nbr [])
                                          yx)})
                    (into rest-work-todo ;; #: Add the estimated path to the todo and recur
                          (map
                           (fn [w]
                             (let [[y x] w]
                               [(total-cost newcost step-est size y x) w]))
                           nbr-yxs)))))))))
 )