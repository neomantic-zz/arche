;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;;  arche - A hypermedia resource discovery service
;;
;;  https://github.com/neomantic/arche
;;
;;  Copyright:
;;    2014
;;
;;  License:
;;    LGPL: http://www.gnu.org/licenses/lgpl.html
;;    EPL: http://www.eclipse.org/org/documents/epl-v10.php
;;    See the LICENSE file in the project's top-level directory for details.
;;
;;  Authors:
;;    * Chad Albers
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defproject arche "0.0.1-SNAPSHOT"
  :description "A hypermedia resource discovery service"
  :url "https://github.com/neomantic/arche"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [liberator "0.12.1"]
                 [compojure "1.1.8"]
                 [ring/ring-core "1.3.1"]
                 [ring/ring-jetty-adapter "1.3.0"]
                 [org.clojure/java.jdbc "0.3.5"]
                 [korma "0.4.0"]
                 [mysql/mysql-connector-java "5.1.25"]
                 [cheshire "5.3.1"]
                 [clj-http "1.0.0"]
                 [environ "1.0.0"]
                 [clj-time "0.8.0"]
                 [pandect "0.3.4"]
                 [inflections "0.9.9"]
                 [clojurewerkz/urly "1.0.0"]
                 [ring-mock "0.1.5"]
                 [speclj "3.1.0"]
                 [pandect "0.3.4"]]
  :profiles {:uberjar {:aot :all}
             :production {:env {:production true}}
             :test {:env {:base-uri "http://example.org"}}
             :dev {:env {:base-uri "http://localhost:3000"}}}
  :licenses [{:name "Eclipse Public License - v 1.0"
              :url "http://www.eclipse.org/legal/epl-v10.html"
              :distribution :repo}
             {:name "GNU Lesser Public License Version 3"
              :url "http://www.gnu.org/licenses/lgpl.html"
              :distribution :repo}]
  :plugins [[lein-cucumber "1.0.2"]
            [ring-serve "0.1.2"]
            [lein-ring "0.8.11"]
            [speclj "3.1.0"]
            [clj-sql-up "0.3.3"]
            [lein-environ "1.0.0"]
            [lein-pprint "1.1.1"]]
  :clj-sql-up {:database {:subprotocol "mysql"}
               :deps [[mysql/mysql-connector-java "5.1.25"]]}
  :min-lein-version "2.0.0"
  :uberjar-name "arche-standalone.jar"
  :ring {:handler arche.core/handler}
  :test-paths ["spec"]
  :cucumber-feature-paths ["features/"]
  :target-path "target/%s")
