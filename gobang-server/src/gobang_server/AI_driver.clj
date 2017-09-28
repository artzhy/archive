(ns gobang-server.AI-driver
  (:require [clojure.core.async :refer [>! <! >!! <!! go chan]]
            [gobang-server.constants :refer [BLACK WHITE SIZE]]
            [gobang-server.common :refer [apply-step gameover?]]))

(defn AI-driver [analyze-func]
  (let [in (chan) out (chan)]
    (go (loop [game-state {:user-side nil :pieces {}}]
          (let [msg (<! in)]
            ;(println "[in]" msg)
            (case (:type msg)
              "START_GAME"
              (let [{side :side} msg]
                (recur
                  (if (= side BLACK)
                    (let [first-step                        ; 第一步总是走正中央
                          {:type "STEP" :side side :point {:x (quot SIZE 2) :y (quot SIZE 2)}}]
                      ;(println "[out]" first-step)
                      (>! out first-step)
                      (-> game-state
                          (assoc :user-side side)
                          (apply-step first-step)))
                    (assoc game-state :user-side side))))

              "STEP"
              (let [step msg
                    next-game-state (apply-step game-state step)]
                (when-not (gameover? (:pieces next-game-state) (:point step))
                  (let [next-step (analyze-func next-game-state)
                        next-game-state (apply-step next-game-state next-step)]
                    ;(println "[out]" (assoc next-step :type "STEP"))
                    (>! out (assoc next-step :type "STEP"))
                    (recur next-game-state))))

              (println "[AI]: unknown message type:" (:type msg))))))
    [in out]))
