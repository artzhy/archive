(ns gobang-server.core
  (:require [org.httpkit.server :refer :all]
            [clojure.core.async :refer [>!! <!! >! <! go chan]]
            [clojure.data.json :as json]

            [gobang-server
             [AI-driver :refer [AI-driver]]
             [common :refer [random-side opponent-side]]
             [constants :refer [BLACK WHITE]]
             [normal-AI :refer [normal-AI]]
             [still-silly-AI :refer [still-silly-AI]]]))

(defmacro use-channel
  "将宏with-channel转化为与let相同的形式, 使cursive更好地进行代码提示"
  [bindings & body]
  (let [channel (bindings 0) request (bindings 1)]
    `(with-channel ~request ~channel ~@body)))

(def idle-pool (atom #{}))
(def waiting-pool (atom #{}))
(def playing-pairs (atom #{}))
(def ai-map (atom {}))

(defn find-opponent [pairs x]
  (when-let [pair (first pairs)]
    (cond
      (= x (first pair)) (second pair)
      (= x (second pair)) (first pair)
      :else (recur (rest pairs) x))))

(defn get-channel-port [channel]
  (re-find #":\d+$" (str channel)))

(def log println)
(defn log-idle-pool! []
  (log "idle-pool:" (set (map get-channel-port @idle-pool))))

(defn log-waiting-pool! []
  (log "waiting-pool:" (set (map get-channel-port @waiting-pool))))

(defn log-playing-pairs! []
  (log "playing-pairs:" (set (map #(set (map get-channel-port %)) @playing-pairs))))

(defn log-ai-map! []
  (log "ai-map's count:" (count @ai-map)))

(defn log-all! []
  (log "--------")
  (log-idle-pool!)
  (log-waiting-pool!)
  (log-playing-pairs!)
  (log-ai-map!)
  (log "--------\n"))

(defn handler [req]
  (use-channel [channel req]
    (on-close channel
              (fn [status]
                ;; todo 似乎有点小问题  最后一个player离开之后, idle-pool中会留下一个nil
                (swap! waiting-pool disj channel)
                (when-let [opponent (find-opponent @playing-pairs channel)]
                  (send! opponent (json/write-str {:type "OPPONENT_LEAVE"}))
                  (swap! playing-pairs disj #{channel opponent})
                  (log "put opponent ot idle-pool")
                  (swap! idle-pool conj opponent))
                (when (contains? @ai-map channel)
                  (swap! ai-map dissoc channel))
                (log "Player leave." (get-channel-port channel) status)
                (log-all!)))

    (when (websocket? channel)
      (log "New player in." (get-channel-port channel))
      (swap! idle-pool conj channel)
      (log-all!))

    (on-receive channel
                (fn [message]
                  ;(log "receive:" message "from" (get-channel-port channel))
                  (let [{:keys [type] :as data} (json/read-str message :key-fn keyword)
                        opponent (find-opponent @playing-pairs channel)]
                    (case type
                      "STEP"
                      (do (when opponent (send! opponent message))
                          (when-let [[in] (get @ai-map channel)]
                            (>!! in data)))

                      "READY"
                      (do
                        (swap! idle-pool disj channel)
                        (swap! waiting-pool conj channel)
                        (let [wp @waiting-pool]
                          (when (= (count wp) 2)
                            (let [x (first wp)
                                  y (second wp)
                                  xside (random-side)
                                  yside (opponent-side xside)]
                              (send! x (json/write-str {:type "START_GAME" :side xside}))
                              (send! y (json/write-str {:type "START_GAME" :side yside}))
                              (reset! waiting-pool #{})
                              (swap! playing-pairs conj #{x y}))))
                        (log-all!))

                      "START_WITH_AI"
                      (do (swap! idle-pool disj channel)
                          (let [[in out :as driver] (AI-driver normal-AI)
                                player-side (random-side)
                                ai-side (opponent-side player-side)]
                            (swap! ai-map assoc channel driver)
                            (send! channel (json/write-str {:type "START_GAME" :side player-side}))
                            (go (>! in {:type "START_GAME" :side ai-side})
                                (while true
                                  (let [step (<! out)]
                                    (send! channel (json/write-str step)))))
                            (log-all!)))

                      "CANCEL_READY"
                      (do
                        (swap! waiting-pool disj channel)
                        (swap! idle-pool conj channel)
                        (log-all!))

                      "GAMEOVER"
                      (do
                        (when-let [opponent (find-opponent @playing-pairs channel)]
                          (swap! playing-pairs disj #{channel opponent})
                          (swap! idle-pool conj opponent))
                        (when (contains? @ai-map channel)
                          (swap! ai-map dissoc channel))
                        (swap! idle-pool conj channel)
                        (log-all!))

                      (log "Invalid type:" type "received")))))))

(run-server handler {:port 12345})
