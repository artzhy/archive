(use 'clojure.pprint)

(defn analyse "problem-124"
  [board side]
  (let [board-size (count board)
        up (fn [[row col]] (when (and row (> row 0)) [(dec row) col]))
        down (fn [[row col]] (when (and row (< row (dec board-size))) [(inc row) col]))
        left (fn [[row col]] (when (and col (> col 0)) [row (dec col)]))
        right (fn [[row col]] (when (and col (< col (dec board-size))) [row (inc col)]))
        up-left (comp up left)
        up-right (comp up right)
        down-left (comp down left)
        down-right (comp down right)
        directions [up down left right up-left up-right down-left down-right]
        get-side (partial get-in board)
        counter? (fn [loc] (distinct? 'e side (get-side loc)))
        same-side? (fn [loc] (= side (get-side loc)))
        available-locs (for [row (range board-size)
                             col (range board-size)
                             :let [loc [row col]]
                             :when (= 'e (get-side loc))] loc)]
    (apply
      merge-with
      #(set (concat %1 %2))
      (for [start-loc available-locs direction directions
            :let [succ-locs (take-while (complement nil?) (next (iterate direction start-loc)))
                  [before after] (split-with counter? succ-locs)
                  valid? (and (seq before)
                              (same-side? (first after)))]
            :when valid?]
        {start-loc (set (take-while counter? succ-locs))}))))

(pprint (analyse '[[e e e e]
                   [e w b e]
                   [e b w e]
                   [e e e e]] 'w))