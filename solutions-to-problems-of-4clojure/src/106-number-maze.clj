(defn number-maze [start target]
  (let [double #(* % 2)
        halve #(when (even? %) (/ % 2))
        add-2 #(+ % 2)
        step (juxt double halve add-2)
        next #(set (filter (complement nil?) (mapcat step %)))]
    (ffirst (filter
              (fn [[_ nums]] (nums target))
              (map vector
                   (iterate inc 1)
                   (iterate next #{start}))))))

(def __ number-maze)
(prn (= 1 (__ 1 1)))                                        ; 1
(prn (= 3 (__ 3 12)))                                       ; 3 6 12
(prn (= 3 (__ 12 3)))                                       ; 12 6 3
(prn (= 3 (__ 5 9)))                                        ; 5 7 9
(prn (= 9 (__ 9 2)))                                        ; 9 18 20 10 12 6 8 4 2
(prn (= 5 (__ 9 12)))                                       ; 9 11 22 24 12