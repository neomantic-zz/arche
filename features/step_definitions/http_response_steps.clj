(ns step-definitions.http-response-steps
  (:use step-definitions.discoverable-resources-steps
        cucumber.runtime.clj
        clojure.test))

(Then #"^the response should have the following header fields:$" [table]
      (let [received-headers (:headers @last-response)
            expected-headers (table-rows-map table)]
        (doall
         (map (fn [header]
                (let [[field value] header]
                  (if (= value "anything")
                    (is (not (nil? value)))
                    (is (= (get received-headers field) value)))))
              expected-headers))))
