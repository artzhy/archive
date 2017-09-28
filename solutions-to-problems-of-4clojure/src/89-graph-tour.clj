(defn graph-tour [g]
  (and
    (<= (count (filter
                 #(odd? (val %))
                 (frequencies (flatten g))))
        2)
    (loop [state #{} index 0]
      (if (< index (count g))
        (let [[x y] (nth g index)
              contains-x-or-y? #(or (contains? % x) (contains? % y))
              without-x-y (set (filter (complement contains-x-or-y?) state))
              with-x-y (into (hash-set x y)
                             (apply concat (filter contains-x-or-y? state)))
              next-state (conj without-x-y with-x-y)]
          (recur next-state (inc index)))
        (= 1 (count state))))))

(def __ graph-tour)
(= true (__ [[:a :b]]))
(= false (__ [[:a :a] [:b :b]]))
(= false (__ [[:a :b] [:a :b] [:a :c] [:c :a]
              [:a :d] [:b :d] [:c :d]]))
(= true (__ [[1 2] [2 3] [3 4] [4 1]]))
(= true (__ [[:a :b] [:a :c] [:c :b] [:a :e]
             [:b :e] [:a :d] [:b :d] [:c :e]
             [:d :e] [:c :f] [:d :f]]))
(= false (__ [[1 2] [2 3] [2 4] [2 5]]))