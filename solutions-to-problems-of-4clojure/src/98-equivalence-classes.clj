;A function f defined on a domain D induces an equivalence relation on D, as follows: a is equivalent to b with respect to f if and only if (f a) is equal to (f b). Write a function with arguments f and D that computes the equivalence classes of D with respect to f.

(defn equivalence-classes [f s]
  (set (map set (vals (group-by f s)))))

(println (equivalence-classes #(* % %) #{-2 -1 0 1 2}))