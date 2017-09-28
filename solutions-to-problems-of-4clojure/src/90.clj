(defn cartesian-product [set1 set2]
  (set (for [item1 set1 item2 set2]
         [item1 item2])))

(println ((defn cartesian-product [set1 set2]
            (set (for [item1 set1 item2 set2]
                   [item1 item2]))) #{1 2 3} #{:a :b :c}))

(defn symmetric-difference "problem/88"
  [set1 set2]
  (set (concat
         (for [item1 set1 :when (not (set2 item1))] item1)
         (for [item2 set2 :when (not (set1 item2))] item2))))

(println (symmetric-difference
           #{1 2 3 4 5 6}
           #{1 3 5 7}))


(defn difference [s1 s2]
  (set
    (filter
      #(not (and (s1 %) (s2 %)))
      (apply merge s1 s2))))

(println (difference #{1 2 3 4 5 6} #{1 3 5 7}))
