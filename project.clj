(defproject wormhole-clj "0.0.1-SNAPSHOT"
  :description "Resource Catalog Hypermedia Service"
  :url "http://bitbucket.org/calbers"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [liberator "0.12.1"]
                 [compojure "1.1.8"]
                 [ring/ring-core "1.3.1"]
                 [ring/ring-jetty-adapter "1.3.0"]
                 [org.clojure/java.jdbc "0.3.5"]
                 [mysql/mysql-connector-java "5.1.25"]]
  :profiles {:uberjar {:aot :all}
             :dev-common {:dependencies [[ring-serve "0.1.2"]
                                         [speclj "3.1.0"]]}
             :dev [:dev-common]}
  :plugins [[lein-cucumber "1.0.2"]
            [lein-ring "0.8.7"]
            [speclj "3.1.0"]
            [clj-sql-up "0.3.3"]]
  :clj-sql-up {
               :deps [[mysql/mysql-connector-java "5.1.25"]]}
  :main ^:skip-aot wormhole-clj.core
  :ring {:handler wormhole-clj.core/handler}
  :test-paths ["spec"]
  :target-path "target/%s"
  :pom-addition [:developers [:developer {:id "neomantic"}
                              [:name "Chad Albers"]
                              [:url "http://www.neomantic.com"]]])
