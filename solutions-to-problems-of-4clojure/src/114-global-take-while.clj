(defn global-take-while
  ([n pred coll] (global-take-while n pred coll nil))
  ([n pred coll last-hit]
   (when (pos? n)
     (let [[before after] (split-with (complement pred) coll)]
       (concat (and last-hit [last-hit])
               before
               (global-take-while (dec n) pred (rest after) (first after)))))))

(let [__ global-take-while]
  (println (= [2 3 5 7 11 13]
              (__ 4 #(= 2 (mod % 3))
                  [2 3 5 7 11 13 17 19 23])))
  (println (= ["this" "is" "a" "sentence"]
              (__ 3 #(some #{\i} %)
                  ["this" "is" "a" "sentence" "i" "wrote"])))
  (println (= ["this" "is"]
              (__ 1 #{"a"}
                  ["this" "is" "a" "sentence" "i" "wrote"]))))