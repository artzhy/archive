; https://www.4clojure.com/problem/111
; Write a function that takes a string and a partially-filled crossword puzzle board,
; and determines if the input string can be legally placed onto the board.
;The crossword puzzle board consists of a collection of partially-filled rows.
; Empty spaces are denoted with an underscore (_), unusable spaces are denoted
; with a hash symbol (#), and pre-filled spaces have a character in place; the
; whitespace characters are for legibility and should be ignored.
; For a word to be legally placed on the board:
;   - It may use empty spaces (underscores)
;   - It may use but must not conflict with any pre-filled characters.
;   - It must not use any unusable spaces (hashes).
;   - There must be no empty spaces (underscores) or extra characters before or after the word (the word may be bound by unusable spaces though).
;   - Characters are not case-sensitive.
(defn crossword-puzzle [target input]
  (let [match? (fn [s1 s2]
                 (and (= (count s1) (count s2))
                      (every? true? (map #(or (= %1 %2) (= \_ %2)) s1 s2))))
        remove-# (partial remove #{[\#]})
        split (partial partition-by #{\#})
        board (map (partial remove #{\space}) input)
        iter-rows board
        iter-cols (apply map vector board)
        candidates (mapcat (comp remove-# split) (concat iter-rows iter-cols))]
    (boolean (some #(match? target %) candidates))))

(println (= true (crossword-puzzle "the" ["_ # _ _ e"])))
(println (= false (crossword-puzzle "the" ["c _ _ _"
                                           "d _ # e"
                                           "r y _ _"])))
(println (= true (crossword-puzzle "joy" ["c _ _ _"
                                          "d _ # e"
                                          "r y _ _"])))
(println (= false (crossword-puzzle "joy" ["c o n j"
                                           "_ _ y _"
                                           "r _ _ #"])))
(println (= true (crossword-puzzle "clojure" ["_ _ _ # j o y"
                                              "_ _ o _ _ _ _"
                                              "_ _ f _ # _ _"])))

