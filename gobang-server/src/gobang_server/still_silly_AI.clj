(ns gobang-server.still-silly-AI
  (:require [gobang-server.constants :refer [SIZE BLACK WHITE]]
            [gobang-server.common :refer [opponent-side]]))


(defn point-to-number [point]
  (+ (* (:x point) SIZE) (:y point)))

(defn number-to-point [number]
  {:x (quot number SIZE) :y (rem number SIZE)})

(def ^:const score-map {:five   100000
                        :live-4 1000
                        :live-3 100
                        :live-2 10
                        :live-1 1
                        :dead-4 100
                        :dead-3 10
                        :dead-2 1
                        :dead-1 0
                        :spam   0})

(def ^:const SQUARE (* SIZE SIZE))
(defn cols [x] (range (* SIZE x) (* SIZE (inc x))))
(defn rows [y] (range y SQUARE SIZE))
(defn diag=y-x [t]
  (let [step (inc SIZE)]
    (if (neg? t)
      (range (* SIZE (- t)) SQUARE step)
      (take (- SIZE t) (range t SQUARE step)))))
(defn diag=y+x [t]
  (let [step (dec SIZE)]
    (if (< t SIZE)
      (take (inc t) (range t SQUARE step))
      (range (dec (* SIZE (+ t (- SIZE) 2))) SQUARE step))))
(def all-lines
  (filter
    #(>= (count %) 5)
    (concat
      (map #(cols %) (range SIZE))
      (map #(rows %) (range SIZE))
      (map #(diag=y-x %) (range (- (dec SIZE)) SIZE))
      (map #(diag=y+x %) (range 0 (dec (* 2 SIZE))))
      )))

(defn live [x]
  (if (>= x 5)
    :five
    (keyword (str "live-" x))))
(defn dead [x]
  (if (>= x 5)
    :five
    (keyword (str "dead-" x))))
(defn double-dead [x]
  (if (>= x 5) :five :spam))

(defn parse-line [items side line]
  (loop [result (zipmap [:five :live-4 :live-3 :live-2 :live-1 :dead-4 :dead-3 :dead-2 :dead-1 :spam] (repeat 0))
         index 0, streak 0, live? false]
    (if (< index (count line))
      (let [item (items (nth line index))]
        (cond
          (nil? item) (if (zero? streak)
                        (recur result (inc index) streak true)
                        (if live?
                          (recur (update result (live streak) inc) (inc index) 0 true)
                          (recur (update result (dead streak) inc) (inc index) 0 true)))
          (= side item) (recur result (inc index) (inc streak) live?)
          (not= side item) (if (zero? streak)
                             (recur result (inc index) streak false)
                             (if live?
                               (recur (update result (dead streak) inc) (inc index) 0 false)
                               (recur (update result (double-dead streak) inc) (inc index) 0 false)))))
      (if (zero? streak)                                    ; 处理最后的一个情况
        result
        (if live?
          (update result (dead streak) inc)
          (update result (double-dead streak) inc))))))
(defn calculate-score [items side]
  (let [parse-result (apply merge-with + (map #(parse-line items side %) all-lines))]
    (reduce (fn [sum key] (+ sum (* (key parse-result) (key score-map))))
            0
            (keys parse-result))))
(defn get-expectation [items]
  (- (calculate-score items BLACK) (calculate-score items WHITE)))

(defn get-valid-points [items]
  (filter #(not (contains? items %)) (range SQUARE)))

(defn get-steps
  ([steps items side depth]
   (let [max? (= side BLACK)
         min-max (if max? max-key min-key)]
     (if (zero? depth)
       (let [step (apply min-max
                         (fn [t] (get-expectation (assoc items t side)))
                         (get-valid-points items))
             _ (prn 'try (count (get-valid-points items)))]
         [(conj steps step) (get-expectation (assoc items step side))])

       (apply min-max second
              (map
                (fn [t] (get-steps (conj steps t) (assoc items t side) (opponent-side side) (dec depth)))
                (get-valid-points items)))))))

(defn still-silly-AI [game-state]
  (let [{:keys [user-side pieces]} game-state
        items (into {} (map (fn [[point side]] [(point-to-number point) side]) pieces))
        [[first-step]] (get-steps [] items user-side 0)]    ; 转换为数字
    {:side  user-side
     :point (number-to-point first-step)}))