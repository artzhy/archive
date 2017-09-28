(ns gobang-server.normal-AI
  (:require [gobang-server.constants :refer [SIZE]]
            [gobang-server.constants :refer [WHITE BLACK SIZE]])
  (:import (gobang_server AI)))

(defn normal-AI [game-state]
  ;(prn game-state)
  (let [[x y] (AI/analyze (:user-side game-state)
                          (for [x (range SIZE)]
                            (for [y (range SIZE)]
                              (get-in game-state [:pieces {:x x :y y}]))))]
    {:side  (:user-side game-state)
     :point {:x x :y y}})

  ;{:side  (:user-side game-state)
  ; :point (first
  ;          (filter #(not (contains? (:pieces game-state) %))
  ;                  (for [x (range SIZE) y (range SIZE)]
  ;                    {:x x :y y})))}
  )
