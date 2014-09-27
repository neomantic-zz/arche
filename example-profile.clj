{:dev
   {:env {:base-uri "http://localhost:3000"
          :database-user "root"
          :database-password ""
          :database-host "localhost"
          :database-name "arche_development"}
    :clj-sql-up {:database {:user "root"
                            :subname "//127.0.0.1:3306/arche_development"
                            :password ""}}}
   :test {:env {:base-uri "http://example.org"
                :database-user "root"
                :database-password ""
                :database-host "localhost"
                :database-name "arche_test"}
          :clj-sql-up {:database {:user "root"
                                  :subname "//127.0.0.1:3306/arche_test"
                                  :password ""}}}}
