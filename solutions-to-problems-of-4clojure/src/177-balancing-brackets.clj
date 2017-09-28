(defn __ [s]
  (let
    [open-brackets ["(" "[" "{"]
     close-brackets [")" "]" "}"]
     bracket-map (zipmap open-brackets close-brackets)
     result
     (reduce
       (fn [stack c]
         (cond
           (= stack false) false

           ((set open-brackets) c) (conj stack c)

           ((set close-brackets) c)
           (if (= c (bracket-map (peek stack)))
             (pop stack) false)

           :else stack))
       []
       (re-seq #"[\(\)\[\]\{\}]" s))]
    (and (not (false? result)) (empty? result))))

(println (__ "This string has no brackets."))
(println (__ "class Test {
      public static void main(String[] args) {
        System.out.println(\"Hello world.\");
      }
    }"))
(println (not (__ "(start, end]")))
(println (not (__ "())")))
(println (not (__ "[ { ] } ")))
(println (__ "([]([(()){()}(()(()))(([[]]({}()))())]((((()()))))))"))
(println (not (__ "([]([(()){()}(()(()))(([[]]({}([)))())]((((()()))))))")))
(println (not (__ "[")))