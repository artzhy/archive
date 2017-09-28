(defn intervals [input]
  (reduce
    (fn [reduction item]
      (cond
        (empty? reduction)
        [[item item]]

        (= (dec item) (nth (last reduction) 1))
        (assoc-in reduction [(dec (count reduction)) 1] item)

        :else (conj reduction [item item])))
    []
    (distinct (sort input))))

(defn my-trampoline [f & args]
  (let [value (apply f args)]
    (loop [r value]
      (if (fn? r)
        (recur (r))
        r))))

(defn calculate "problem-121"
  [expr]
  (fn [m]
    (cond
      (symbol? expr) (m expr)
      (number? expr) expr
      :else (apply ({'+ + '- - '* * '/ /} (first expr))
                   (map #((calculate %) m) (rest expr))))))


;; problem-116
(let [is-prime? (fn [x] (not-any? #(= 0 (mod x %)) (range 2 (inc (Math/sqrt x)))))
      prime-range (filter is-prime? (drop 2 (range)))]
  (fn is-balanced-prime? [x]
    (if
      (<= x 2)
      false
      (let [[left right] (split-with #(< % x) prime-range)
            before (or (last left) 0)
            after (second right)]
        (and (is-prime? x) (= x (/ (+ before after) 2)))))))