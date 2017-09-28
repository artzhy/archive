(defn __ [pred seprator coll]
  (filter
    #(not= :pad %)
    (interleave
      coll
      (concat
        (map {true seprator false :pad}
             (map #(apply pred %) (partition 2 1 coll)))
        [:pad]))))

(println (= '(1 :less 6 :less 7 4 3) (__ < :less [1 6 7 4 3])))
(println (= '(2) (__ > :more [2])))
(println (= [0 1 :x 2 :x 3 :x 4] (__ #(and (pos? %) (< % %2)) :x (range 5))))
(println (empty? (__ > :more ())))
(println (= [0 1 :same 1 2 3 :same 5 8 13 :same 21]
            (take 12
                  (->> [0 1]
                       (iterate (fn [[a b]] [b (+ a b)]))
                       (map first)                          ; fibonacci numbers
                       (__ (fn [a b]                        ; both even or both odd
                             (= (mod a 2) (mod b 2)))
                           :same)))))