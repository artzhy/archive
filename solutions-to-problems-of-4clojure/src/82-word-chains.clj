(defn word-chains? [words]
  (letfn [(insertion? [a b]
            (and
              (= 1 (- (count b) (count a)))
              (let [va (vec a)
                    vb (vec b)
                    n (loop [i 0]
                        (if (and (< i (count a)) (= (nth a i) (nth b i)))
                          (recur (inc i))
                          i))]
                (= (subvec va n) (subvec vb (inc n))))))
          (deletion? [a b] (insertion? b a))
          (substitution? [a b]
            (and
              (= (count a) (count b))
              (let [va (vec a)
                    vb (vec b)]
                (= (dec (count a))
                   (count (filter true? (map = va vb)))))))
          (can-chain? [a b]
            (or (insertion? a b) (deletion? a b) (substitution? a b)))]
    (let [words-vec (vec words)
          n (count words-vec)
          m (set (let [words-vec (vec words)
                       n (count words-vec)]
                   (for [i (range n)
                         j (range (inc i) n)
                         :let [w1 (nth words-vec i) w2 (nth words-vec j)]
                         :when (and (not= w1 w2) (can-chain? w1 w2))]
                     #{w1 w2})))]
      (letfn
        [(chain-all? [chained remaining]
           (or (= n (count chained))
               (some
                 true?
                 (for [word remaining
                       :when (or (empty? chained) (contains? m #{(last chained) word}))]
                   (chain-all? (conj chained word) (disj remaining word))))))]
        (boolean (chain-all? [] (set words)))))))

(def __ word-chains?)
(prn (= true (__ #{"hat" "coat" "dog" "cat" "oat" "cot" "hot" "hog"})))
(prn (= false (__ #{"cot" "hot" "bat" "fat"})))
(prn (= false (__ #{"to" "top" "stop" "tops" "toss"})))
(prn (= true (__ #{"spout" "do" "pot" "pout" "spot" "dot"})))
(prn (= true (__ #{"share" "hares" "shares" "hare" "are"})))
(prn (= false (__ #{"share" "hares" "hare" "are"})))