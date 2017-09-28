; todo 这个文件专门用来测试AI

(ns gobang-server.AI-test
  (:require [gobang-server.normal_AI :refer [get-steps]]
            [gobang-server.constants :refer [BLACK WHITE]]))

(def items-1 {0   "BLACK",
              128 "WHITE",
              7   "WHITE",
              1   "BLACK",
              4   "WHITE",
              129 "WHITE",
              131 "WHITE",
              6   "BLACK",
              146 "WHITE",
              3   "BLACK",
              2   "BLACK",
              127 "WHITE",
              5   "BLACK",
              112 "BLACK",
              161 "WHITE",
              8   "BLACK",
              9   "BLACK"})

(time (get-steps [] items-1 WHITE 0))