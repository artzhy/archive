(defn disjoint? [ss]
  (let [is-disjoint?
        (fn [m n] (every? false? (for [x m] (contains? n x))))]
    (every?
      true?
      (for [m ss
            n ss
            :when (not= m n)]
        (is-disjoint? m n)))))

(def __ disjoint?)


;(println (disjoint? #{#{\U} #{\s} #{\e \R \E} #{\P \L} #{\.}}))
;(println (__ #{#{:a :b :c :d :e}
;               #{:a :b :c :d}
;               #{:a :b :c}
;               #{:a :b}
;               #{:a}}))

(defn into-camel-case [s]
  (let [v (clojure.string/split s #"-\b")
        up-first-letter
        (fn [cs]
          (apply str (.toUpperCase (str (first cs))) (rest cs)))]
    (apply str (first v) (map up-first-letter (rest v)))))

(defn identify-keys-and-values [xs]
  (if (empty? xs)
    {}
    (let [[[key] rest] (split-at 1 xs)
          [value rest] (split-with (complement keyword?) rest)
          value (vec value)]
      (into {key value} (identify-keys-and-values rest)))))
; a more elegant solution to problem-105
;#(->>
;  %
;  (partition-by keyword?)
;  (mapcat (fn [[k :as v]] (if (keyword? k) (interpose [] v) [v])))
;  (apply hash-map))

(defn pronunciation-sequence [ns]
  (letfn [(single [xs] [(count xs) (first xs)])
          (pro [coll] (mapcat single (partition-by identity coll)))]
    (iterate pro (pro ns))))

(defn decurry [f]
  (fn [& args]
    (reduce #(%1 %2) f args)))

(defn my-reductions [f val coll]
  (if (empty? coll)
    [val]
    (lazy-seq
      (cons val (my-reductions f (f val (first coll)) (rest coll))))))

(defn oscilrate [value & fs]
  (reductions (fn [v f] (f v))
              value (cycle fs)))

(defn partially-flatten [s]
  (letfn [(simple-coll? [ss]
            (and (coll? ss)
                 (every? false? (map coll? ss))))]
    (filter simple-coll?
            (tree-seq coll? seq s))))

(defn lazy-searching [& colls]
  (letfn [(min-index [coll]
            (get (apply min-key #(% 1) (map vector (range) coll)) 0))
          (update [m k f]
            (assoc m k (f (get m k))))]
    (loop [cs (vec colls)]
      (let [heads (map first cs)
            found (apply = heads)
            i (min-index (map first cs))
            updated (update cs i rest)]
        (if
          found
          (first heads)
          (recur updated))))))

;(println "lazy-searching:"
;         (lazy-searching [3 4 5]))
;(println "lazy-searching:"
;         (lazy-searching (range) (range 0 100 7/6) [2 3 5 7 11 13]))


;todo 这个函数有bug，problem-114
(defn global-take-while [n pred coll]
  (let [partitioned (partition-by pred coll)
        splited (mapcat (fn [c]
                          (if (pred (first c))
                            (mapcat vector c (repeat nil))
                            c)) partitioned)
        _ (println splited)
        filtered (filter (complement nil?) splited)
        _ (println filtered)]
    (take
      (- (* 2 n) (if (pred (first filtered)) 2 1))
      filtered)))

(println (global-take-while
           4 #(= 2 (mod % 3))
           [2 3 5 7 11 13 17 19 23]))
(println (global-take-while
           3 #(some #{\i} %)
           ["this" "is" "a" "sentence" "i" "wrote"]))
(println (global-take-while
           1 #{"a"}
           ["this" "is" "a" "sentence" "i" "wrote"]))