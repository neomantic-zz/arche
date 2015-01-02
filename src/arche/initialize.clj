;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;;  arche - A hypermedia resource discovery service
;;
;;  https://github.com/neomantic/arche
;;
;;  Copyright:
;;    2015
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

(ns arche.initialize
  (:require [arche.resources.discoverable-resource :refer :all]
            [arche.resources.discoverable-resources-collection :refer :all]
            [arche.validations :refer :all]
            [korma.core :refer :all]))

(defn seed-entry-point
  "Seeds the database with Arche's entry point for discoverable resources"
  []
  (letfn [(create-it [attributes]
            (let [validated (validate attributes
                                      [validate-href
                                       validate-link-relation
                                       validate-resource-name-present])]
              (if (-> validated has-errors?)
                (throw (Exception. (str "Unable to seed Arche's entry point with: " validated)))
                (discoverable-resource-create
                 (assoc attributes :link-relation-url (:link_relation_url attributes)
                        :resource-name (:resource_name attributes))))))]
    (let [exists (discoverable-resource-first (:routable names))
          attributes {:link_relation_url profile-url
                      :resource_name (:routable names)
                      :href self-url}]
      (if (not (nil? exists))
        (if (and (= (:link_relation_url exists) (:link_relation_url attributes))
                 (= (:href exists) (:href attributes)))
          exists
          (do
            (delete discoverable-resources (where {:resource_name [= (:routable names)]}))
            (create-it attributes)))
        (create-it attributes)))))
