(defn pascal-triangle [n]
  (if (= n 1)
    [1]
    (let [m (conj (pascal-triangle (dec n)) 0)
          n (vec (cons 0 (pascal-triangle (dec n))))]
      (vec
        (for [index (range (count m))]
          (+ (m index) (n index)))))))
