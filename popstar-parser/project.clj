(defproject popstar-parser "0.1.0"
  :description "Popstar game parser."
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.json "0.2.6"]]
  :main ^:skip-aot popstar-parser.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
