;Create a function which accepts as input a boolean algebra function in the form of a set of sets, where the inner sets are collections of symbols corresponding to the input boolean variables which satisfy the function (the inputs of the inner sets are conjoint, and the sets themselves are disjoint... also known as canonical minterms). Note: capitalized symbols represent truth, and lower-case symbols represent negation of the inputs. Your function must return the minimal function which is logically equivalent to the input.
;PS — You may want to give this a read before proceeding: K-Maps

(defn subset? [s1 s2]
  (let [result (every? true? (map #(contains? s2 %) s1))]
    (println "subset:" s1 s2 result)
    result))

(defn opposite? [x y]
  (and (not= x y) (= (.toLowerCase (str x)) (.toLowerCase (str y)))))

(defn can-merge? [m n]
  (let [result (or (subset? m n)
                   (some true? (for [term m]
                                 (and (subset? (disj m term) n)
                                      (boolean (some #(opposite? term %) n))))))]
    ;(println "testing:" m n)
    ;(println "result:" result)
    result))

(defn merge-form [m n]
  (cond
    (subset? m n) [m]
    :else
    (let [term (some (complement nil?)
                     (for [term m]
                       (when (and (subset? (disj m term) n)
                                  (some #(opposite? term %) n))
                         term)))]
      [m (remove #(opposite? term %) n)])))

(defn veitch [forms]
  (println forms)
  (if-let [[m n] (some #(apply can-merge? %)
                       (for [m forms n forms :when (not= m n)] [m n]))]
    (recur (concat (disj forms m n) (merge-form m n)))
    forms))

(println (veitch #{#{'A 'b 'c 'd}
                   #{'A 'b 'c 'D}}))