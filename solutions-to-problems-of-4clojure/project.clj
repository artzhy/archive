(defproject solutions-to-problems-of-4clojure "0.1.0"
  :description "Solutions to problems of 4clojure.com."
  :dependencies [[org.clojure/clojure "1.8.0"]]
  :main ^:skip-aot popstar-parser.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
