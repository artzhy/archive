(ns gobang-server.common
  (:require [gobang-server.constants :refer [SIZE BLACK WHITE]]))

(defn random-side []
  (if (> (Math/random) 0.5)
    BLACK WHITE))

(defn opponent-side [side]
  (if (= side BLACK)
    WHITE BLACK))

(defn apply-step [game-state step]
  (assoc-in game-state [:pieces (:point step)] (:side step)))

(def directions ^:const [[-1 0] [-1 -1] [0 -1] [1 -1]])
(defn forward ^:inline [[dx dy] point]
  (let [{:keys [x y]} point]
    {:x (+ x dx)
     :y (+ y dy)}))
(defn backward ^:inline [[dx dy] point]
  (let [{:keys [x y]} point]
    {:x (- x dx)
     :y (- y dy)}))

(defn gameover? [pieces last-point]
  (or (= (* SIZE SIZE) (count pieces))
      (let [side (pieces last-point)]
        (some #(>= % 4)
              (map (fn [direction]
                     (let
                       [next-point (partial forward direction)
                        prev-point (partial backward direction)
                        forward-seq (next (iterate next-point last-point))
                        backward-seq (next (iterate prev-point last-point))]
                       (+ (count (take-while #(= side (pieces %)) forward-seq))
                          (count (take-while #(= side (pieces %)) backward-seq)))))
                   directions)))))
