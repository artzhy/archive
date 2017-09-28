(defn graph-connectivity? [edges]
  (let [m (zipmap (flatten (vec edges)) (repeat nil))
        find (fn [m vertex] (if (nil? (m vertex))
                              vertex
                              (recur m (m vertex))))]
    (= 1 (count
           (filter
             nil?
             (vals
               (reduce
                 (fn [m [va vb]]
                   (let [a (find m va)
                         b (find m vb)]
                     (if (not= a b) (assoc m a b) m)))
                 m edges)))))))
(def __ graph-connectivity?)

(= true (__ #{[:a :a]}))
(= true (__ #{[:a :b]}))
(= false (__ #{[1 2] [2 3] [3 1]
               [4 5] [5 6] [6 4]}))
(= true (__ #{[1 2] [2 3] [3 1]
              [4 5] [5 6] [6 4] [3 4]}))
(= false (__ #{[:a :b] [:b :c] [:c :d]
               [:x :y] [:d :a] [:b :e]}))
(= true (__ #{[:a :b] [:b :c] [:c :d]
              [:x :y] [:d :a] [:b :e] [:x :a]}))
