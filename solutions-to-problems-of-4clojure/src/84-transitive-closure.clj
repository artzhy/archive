(defn transitive-closure [input]
  (loop [result #{} in input]
    (if (seq in)
      (let [[x y] (first in)
            set-A (for [[mx my] result :when (= my x)] [mx y])
            set-B (for [[nx ny] result :when (= nx y)] [x ny])
            set-C (for [[mx my] result
                        :when (= my x)
                        [nx ny] result
                        :when (= nx y)]
                    [mx ny])]
        (recur (conj (into result (concat set-A set-B set-C)) [x y])
               (rest in)))
      result)))

(def __ transitive-closure)
(println (let [divides #{[8 4] [9 3] [4 2] [27 9]}]
           (= (__ divides) #{[4 2] [8 4] [8 2] [9 3] [27 9] [27 3]})))

(println (let [more-legs
               #{["cat" "man"] ["man" "snake"] ["spider" "cat"]}]
           (= (__ more-legs)
              #{["cat" "man"] ["cat" "snake"] ["man" "snake"]
                ["spider" "cat"] ["spider" "man"] ["spider" "snake"]})))

(println (let [progeny
               #{["father" "son"] ["uncle" "cousin"] ["son" "grandson"]}]
           (= (__ progeny)
              #{["father" "son"] ["father" "grandson"]
                ["uncle" "cousin"] ["son" "grandson"]})))
