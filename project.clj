(defproject wormhole-clj "0.0.1-SNAPSHOT"
  :description "Resource Catalog Hypermedia Service"
  :url "http://bitbucket.org/calbers"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [liberator "0.12.1"]
                 [compojure "1.1.8"]
                 [ring/ring-core "1.3.1"]
                 [ring/ring-jetty-adapter "1.3.0"]]
  :dev-dependencies [[ring-serve "0.1.2"]]
  :plugins [[lein-ring "0.8.7"]]
  :main ^:skip-aot wormhole-clj.core
  :ring {:handler wormhole-clj.core/handler}
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
