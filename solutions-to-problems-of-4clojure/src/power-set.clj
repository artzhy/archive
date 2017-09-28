(defn power-set [s]
  (if (empty? s)
    #{#{}}
    (let [small (power-set (rest s))
          other (for [ss small] (conj ss (first s)))]
      (set (concat small other)))))

(defn product-digits [x y]
  (->> (* x y)
       str
       seq
       (map str)
       (map #(Integer/parseInt %))
       vec))

(defn infix-calculator [r & op-num-list]
  (reduce (fn [r [op num]] (op r num))
          r (partition 2 op-num-list)))

(defn indexing-sequences [coll]
  (map-indexed (fn [i x] [x i]) coll))

(def indexing-sequences-2 #(seq (zipmap % (range))))

(defn is-tree?
  [coll]
  (let [v (vec coll)
        left (get v 1)
        right (get v 2)
        is-subtree? #(or (nil? %) (and (coll? %) (is-tree? %)))]
    (and (= (count v) 3)
         (is-subtree? left)
         (is-subtree? right))))

(defn my-map [f xs]
  (lazy-seq
    (when-let [s (seq xs)]
      (cons (f (first s)) (my-map f (rest s))))))

(defn sum-of-digits [num]
  (->> num str seq
       (map str)
       (map #(Integer/parseInt %))
       (map #(* % %))
       (apply +)))

(defn problem-120 [nums]
  (count
    (filter
      (fn [num]
        (< num
           (->> num str seq
                (map str)
                (map #(Integer/parseInt %))
                (map #(* % %))
                (apply +))))
      nums)))

(defn problem-128 [s]
  (let [suit-map {\S :spade \D :diamond \H :heart \C :club}
        rank-map (zipmap "23456789TJQKA" (range 13))
        suit (get s 0)
        rank (get s 1)]
    {:suit (suit-map suit) :rank (rank-map rank)}))

(defn problem-147 [start]
  (iterate
    (fn [xs]
      (map +'
           (concat [0] xs)
           (concat xs [0]))
      )
    start))

(defn problem-77 [words]
  (let [eq? (fn [word] (frequencies (set word)))]
    (set
      (filter
        #(> (count %) 1)
        (map set (vals (group-by eq? words)))))))

(defn problem-70 [s]
  (let [to-int-seq (fn [w] (map int (.toLowerCase w)))
        cmp (fn cmp [mstr nstr]
              (let [m (to-int-seq mstr)
                    n (to-int-seq nstr)
                    mm (first m)
                    nn (first n)]
                (cond
                  (or (empty? m) (< mm nn)) -1
                  (or (empty? n) (> mm nn)) 1
                  :else (cmp (subs mstr 1) (subs nstr 1)))
                ))]
    (sort cmp (re-seq #"\w+" s))))

(def __ problem-70)