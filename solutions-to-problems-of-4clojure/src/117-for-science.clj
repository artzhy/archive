(defn for-science [input]
  (let [matrix (vec (map vec input))
        height (count matrix)
        width (count (first matrix))
        index-of (fn [coll pred]
                   (loop [index 0]
                     (if (= index (count coll))
                       -1
                       (if (pred (nth coll index))
                         index
                         (recur (inc index))))))
        valid (fn [[row col]] (and (<= 0 row)
                                   (< row height)
                                   (<= 0 col)
                                   (< col width)
                                   (not= \# (get-in matrix [row col]))))
        around (fn [[row col]] (filter valid [[(dec row) col]
                                              [(inc row) col]
                                              [row (dec col)]
                                              [row (inc col)]]))
        start-row (index-of matrix #(contains? (set %) \M))
        start-col (index-of (nth matrix start-row) #(= \M %))
        start [start-row start-col]
        end-row (index-of matrix #(contains? (set %) \C))
        end-col (index-of (nth matrix end-row) #(= \C %))
        end [end-row end-col]
        region (loop [region #{start}]
                 (let [expand (into region (mapcat around region))]
                   (if (= expand region)
                     region
                     (recur expand))))]
    (contains? (set region) end)))

(def __ for-science)
(= true (__ ["M   C"]))
(= false (__ ["M # C"]))
(= true (__ ["#######"
             "#     #"
             "#  #  #"
             "#M # C#"
             "#######"]))
(= false (__ ["########"
              "#M  #  #"
              "#   #  #"
              "# # #  #"
              "#   #  #"
              "#  #   #"
              "#  # # #"
              "#  #   #"
              "#  #  C#"
              "########"]))
(= false (__ ["M     "
              "      "
              "      "
              "      "
              "    ##"
              "    #C"]))
(= true (__ ["C######"
             " #     "
             " #   # "
             " #   #M"
             "     # "]))
(= true (__ ["C# # # #"
             "        "
             "# # # # "
             "        "
             " # # # #"
             "        "
             "# # # #M"]))
