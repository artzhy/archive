(ns popstar-parser.utils
  (:require [clojure.set :refer [difference]]
            [clojure.pprint :refer [pprint]]))

(def SIZE 10)
(def DEEPEST (* SIZE SIZE))

(defstruct Snapshot :score :stars)

(defn from-x-y [x y] (+ (* x 10) y))
(defn get-x [point] (quot point SIZE))
(defn get-y [point] (rem point SIZE))

(defn deepest? [depth]
  (= depth DEEPEST))

(defn- dec-depth [depth]
  (if (deepest? depth)
    DEEPEST (dec depth)))

(defn around
  "得到一个点的周围四个点的集合"
  [p]
  (filter
    boolean
    #{(when-not (zero? (rem p SIZE)) (dec p))
      (when-not (zero? (rem (inc p) SIZE)) (inc p))
      (+ p SIZE)
      (- p SIZE)}))

(defn find-group
  "得到一个点所在的group"
  [stars anchor]
  (let [color (stars anchor)]
    (loop [result #{} new #{anchor}]
      (let [next (set (filter
                        #(and (= color (stars %))
                              (not (contains? result %))
                              (not (contains? new %)))
                        (mapcat around new)))
            result (into result new)]
        (if (empty? next)
          result
          (recur result next))))))

(defn get-score [n]
  (* 5 n n))

(defn get-bonus [n]
  (Math/max 0 (- 2000 (* 20 n n))))

(def get-groups
  (memoize
    (fn [stars]
      (if (empty? stars)
        ; 注意考虑stars为空的情况, 在情况下所有星星被消除
        #{}
        (loop [points-to-traverse (set (keys stars))
               result #{}]
          (let [anchor (first points-to-traverse)
                group (find-group stars anchor)
                result (conj result group)
                remaining (difference points-to-traverse group)]
            (if (zero? (count remaining))
              result
              (recur remaining result))))))))

(defn get-valid-groups [stars]
  "从stars中得到group集合"
  (set (filter #(> (count %) 1) (get-groups stars))))

(defn get-single-points [stars]
  (map first (filter #(= (count %) 1) (get-groups stars))))

(defn arrange-stars
  "重新排列stars"
  [stars]
  (let [down-map
        (vec (for [x (range SIZE)]
               (let [is-in-stars
                     (vec (for [y (range SIZE)]
                            (contains? stars (from-x-y x y))))]
                 (vec (for [i (range SIZE)]
                        (count (filter false? (subvec is-in-stars 0 i))))))))
        left-map
        (let [is-valid-column
              (vec (for [x (range SIZE)]
                     (some (fn [[point]] (= (get-x point) x)) stars)))]
          (vec (for [x (range SIZE)]
                 (count (filter not (subvec is-valid-column 0 x))))))]
    (map
      (fn [[point color]]
        (let [x (get-x point)
              y (get-y point)]
          [(from-x-y
             (- x (left-map x))
             (- y ((down-map x) y)))
           color]))
      stars)))

(defn game-over?
  "判断游戏是否结束"
  [snapshot]
  (zero? (count (get-valid-groups (:stars snapshot)))))

(defn remove-group
  "从snapshot移除一个group, 并调用arrange-stars重新整理星星"
  [snapshot group]
  (-> snapshot
      (update :score + (get-score (count group)))
      (update :stars
              (fn [stars]
                (into {} (filter
                           (fn [[point]]
                             (not (contains? group point)))
                           stars))))
      (update :stars
              (fn [stars]
                (into {} (arrange-stars stars))))))

(defn remove-point
  "从snapshot移除点所在的集合, 并调用arrange-stars重新整理星星"
  [snapshot point]
  (remove-group snapshot (find-group (:stars snapshot) point)))

(defn lonely? [stars point]
  (let [has-same-color #(= (stars %) (stars point))
        x (get-x point)
        y (get-y point)
        related-points (for [xi [(dec x) x (inc x)]
                             yi (range SIZE)
                             :when (or (not= xi x) (not= yi y))]
                         (from-x-y xi yi))]
    (not-any? has-same-color related-points)))

(defn calculate-penalty [stars panelty]
  (if (<= (count stars) SIZE)
    0
    (let [single-points (get-single-points stars)]
      (max (* panelty SIZE
              (count (filter (partial lonely? stars) single-points)))
           (* panelty SIZE SIZE)))))

(defn get-expectation
  "获取snapshot的期望值"
  [{:keys [score stars]} panelty]
  (let [groups (get-valid-groups stars)]
    (apply + score
           (get-bonus (count stars))
           (if (zero? (count groups))
             0
             (calculate-penalty stars panelty))
           (map (comp get-score count) groups))))

(defn get-xs [group]
  (set (map get-x group)))

(defn get-related-xs [snapshot step]
  (if (nil? snapshot)
    #{}
    (get-xs (find-group (:stars snapshot) step))))

(defn distance [xs-1 xs-2]
  (if (empty? xs-1)
    0
    (let [min-x1 (apply min xs-1)
          max-x1 (apply max xs-1)
          min-x2 (apply min xs-2)
          max-x2 (apply max xs-2)]
      (cond
        (> min-x1 max-x2) (- min-x1 max-x2)
        (< max-x1 min-x2) (- min-x2 max-x1)
        :else 0))))

(defn get-choices
  "获取从一个snapshot开始, depth步以内的所有情况"
  [steps last-snapshot snapshot depth]
  (let [last-related-xs
        (get-related-xs last-snapshot (last steps))

        close-with-last-step?
        #(<= (distance last-related-xs (get-xs %))
             (- SIZE (int (Math/sqrt (count (:stars snapshot))))))

        groups
        (get-valid-groups (:stars snapshot))]
    (if (or
          (zero? depth)
          (zero? (count groups)))
      [[snapshot steps]]
      (into
        {}
        (mapcat
          (fn [group]
            (get-choices (conj steps (first group))
                         snapshot
                         (remove-group snapshot group)
                         (dec-depth depth)))
          (filter close-with-last-step? groups))))))

(defn choose-depth [snapshot]
  "根据snapshot的情况选取合适的depth值"
  (let [group-count (count (get-valid-groups (:stars snapshot)))
        star-count (count (:stars snapshot))]
    (cond
      (or (>= group-count 14) (>= star-count 80)) 4
      (or (>= group-count 8) (>= star-count 50)) 5
      :else DEEPEST)))

(defn get-best-choice
  "计算snapshot的下一步"
  [snapshot panelty]
  (let [depth (choose-depth snapshot)
        choices (time (get-choices [] nil snapshot depth))
        _ (println "Examined" (count choices) "choices")
        expectation-map
        (map (fn [[snapshot steps]]
               {:steps       steps
                :expectation (get-expectation snapshot panelty)})
             choices)
        best-choice (apply max-key :expectation expectation-map)]
    (assoc best-choice :depth depth)))