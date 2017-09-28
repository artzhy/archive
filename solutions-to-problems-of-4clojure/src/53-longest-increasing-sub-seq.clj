(fn [xs]
  (or
    (apply max-key count
           (->> (map #(conj (vec (map first %)) (last (last %)))
                     (partition-by (partial apply <) (partition 2 1 xs)))
                (filter (partial apply <))
                reverse
                seq))
    []))


;; Below is copied from other solutions.
(fn [v]
  (or
    (first
      (filter #(apply < %)
              (mapcat
                #(partition % 1 v)
                (range (count v) 1 -1))))
    []))