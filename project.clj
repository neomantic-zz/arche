(defproject wormhole-clj "0.0.1-SNAPSHOT"
  :description "Resource Catalog Hypermedia Service"
  :url "http://bitbucket.org/calbers"
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :main ^:skip-aot wormhole-clj.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
