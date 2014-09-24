(defproject arche "0.0.1-SNAPSHOT"
  :description "Resource Catalog Hypermedia Service"
  :url "http://bitbucket.org/calbers"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [liberator "0.12.1"]
                 [compojure "1.1.8"]
                 [ring/ring-core "1.3.1"]
                 [ring/ring-jetty-adapter "1.3.0"]
                 [org.clojure/java.jdbc "0.3.5"]
                 [korma "0.4.0"]
                 [mysql/mysql-connector-java "5.1.25"]
                 [cheshire "5.3.1"]
                 [clj-http "1.0.0"] ;; FIX - this is really a development dependency
                 [environ "1.0.0"]
                 [clj-time "0.8.0"]
                 [pandect "0.3.4"]
                 [inflections "0.9.9"]
                 [clojurewerkz/urly "1.0.0"]
                 [ring-mock "0.1.5"]
                 [pandect "0.3.4"]]
  :profiles {:uberjar {:aot :all}
             :spec {:env {:base-uri "http://example.org"}
                    :dependencies [[speclj "3.1.0"]]}
             :test {:env {:base-uri "http://example.org"}}
             :dev {:env {:base-uri "http://localhost:3000"}
                   :dependencies [[speclj "3.1.0"]]}}
  :plugins [[lein-cucumber "1.0.2"]
            [ring-serve "0.1.2"]
            [lein-ring "0.8.11"]
            [speclj "3.1.0"]
            [clj-sql-up "0.3.3"]
            [lein-environ "1.0.0"]
            [lein-pprint "1.1.1"]]
  :clj-sql-up {:database {:subprotocol "mysql"}
               :deps [[mysql/mysql-connector-java "5.1.25"]]}
  :main ^:skip-aot arche.core
  :ring {:handler arche.core/handler}
  :test-paths ["spec"]
  :cucumber-feature-paths ["features/"]
  :target-path "target/%s"
  :pom-addition [:developers [:developer {:id "neomantic"}
                              [:name "Chad Albers"]
                              [:url "http://www.neomantic.com"]]])
