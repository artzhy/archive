(ns popstar-parser.core
  (:gen-class)
  (:require [clojure.data.json :as json]
            [popstar-parser.utils
             :refer :all]
            [clojure.pprint :refer [pprint]]))

(defn json-to-snapshot [{:keys [stars score]}]
  (struct Snapshot
          score
          (into
            {}
            (filter
              (fn [[_ v]] (not= v :O))
              (zipmap
                (map (fn [index]
                       (let [row (quot index SIZE)
                             column (mod index SIZE)
                             x column
                             y (- SIZE 1 row)]
                         (from-x-y x y)))
                     (range (* SIZE SIZE)))
                (mapcat (fn [color-string]
                          (map (comp keyword str) color-string))
                        stars))))))

(defn analyze [snapshot picks panelty]
  (let [{:keys [steps depth] :as best-choice}
        (get-best-choice snapshot panelty)
        next-snapshot (if (deepest? depth)
                        (reduce remove-point snapshot steps)
                        (remove-point snapshot (first steps)))
        _ (pprint (assoc best-choice
                    :star-count (count (:stars next-snapshot))
                    :group-count (count (get-valid-groups (:stars next-snapshot)))))
        _ (println)]
    (if (game-over? next-snapshot)
      (concat picks steps)
      (recur next-snapshot (conj picks (first steps)) panelty))))

(defn print-steps [steps]
  (pprint
    (for [step steps]
      (let [x (get-x step)
            y (get-y step)]
        {:x x :y y}))))

(defn -main [& args]
  (let [filename (or (first args) "input.json")
        panelty (Integer/parseInt (or (second args) "-2"))
        _ (println "filename:" filename "  panelty:" panelty)
        snapshot (json-to-snapshot
                   (json/read-str
                     (slurp filename)
                     :key-fn keyword))]
    (print-steps (time (analyze snapshot [] panelty)))))
