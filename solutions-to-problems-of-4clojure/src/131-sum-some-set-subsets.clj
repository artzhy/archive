; Given a variable number of sets of integers, create a function which returns true iff all of the sets have a non-empty subset with an equivalent summation.

(defn sum-some-set-subsets [& sets]
  (let [power-set (fn power-set [s]
                    (if (empty? s)
                      #{#{}}
                      (let [small (power-set (rest s))
                            other (for [ss small] (conj ss (first s)))]
                        (set (concat small other)))))
        sum-sets (for [s sets]
                   (set (map (partial apply +) (filter #(seq %) (power-set s)))))]
    (boolean
      (or (= 1 (count sets))
          (some true? (for [x (first sum-sets)]
                        (every? #(% x) (rest sum-sets))))))))

(println (= true (sum-some-set-subsets #{-1 1 99}
                                       #{-2 2 888}
                                       #{-3 3 7777})))
(println (= false (sum-some-set-subsets #{1}
                                        #{2}
                                        #{3}
                                        #{4})))
(println (= true (sum-some-set-subsets #{1})))
(println (= false (sum-some-set-subsets #{1 -3 51 9}
                                        #{0}
                                        #{9 2 81 33})))
(println (= true (sum-some-set-subsets #{1 3 5}
                                       #{9 11 4}
                                       #{-3 12 3}
                                       #{-3 4 -2 10})))
(println (= false (sum-some-set-subsets #{-1 -2 -3 -4 -5 -6}
                                        #{1 2 3 4 5 6 7 8 9})))
(println (= true (sum-some-set-subsets #{1 3 5 7}
                                       #{2 4 6 8})))
(println (= true (sum-some-set-subsets #{-1 3 -5 7 -9 11 -13 15}
                                       #{1 -3 5 -7 9 -11 13 -15}
                                       #{1 -1 2 -2 4 -4 8 -8})))
(println (= true (sum-some-set-subsets #{-10 9 -8 7 -6 5 -4 3 -2 1}
                                       #{10 -9 8 -7 6 -5 4 -3 2 -1})))