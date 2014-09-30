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

(ns arche.resources.discoverable-resources-collection
  (:require [liberator.core :refer [resource defresource]]
            [cheshire.core :refer :all :as json]
            [clojure.string :only (join) :as str]
            [clojure.java.io :as io]
            [arche.resources.discoverable-resource
             :refer :all :as entity]
            [arche.resources.core :refer :all :as generic]
            [arche.media :as media]))

(defn method-supports-body? [ctx]
  (#{:put :post} (get-in ctx [:request :request-method])))

(defn includes_required? [parsed]
  (not (#(some nil? %) (map #(get parsed %) entity/required-descriptors))))

(def supported-content-types ["application/json"])

(defresource discoverable-resources-collection [request]
  :allowed-methods [:post]
  :available-media-types [media/hal-media-type]
  :handle-not-acceptable generic/not-acceptable-response
  :malformed? (fn [ctx]
                (when (method-supports-body? ctx)
                  (try
                    (if-let [body (get-in ctx [:request :body])]
                      [false {::parsed
                              (json/parse-string
                               (condp instance? body
                                 java.lang.String body
                                 (slurp (io/reader body))) true)}]
                      [true {:message "No Body"}])
                    (catch Exception e
                      [true {:message "Required valid content for Content-Type applicaton/json"}]))))
  :known-content-type? (fn [ctx]
                         (if (not (some #{(get-in ctx [:request :headers "content-type"])} ["application/json"]))
                           [false {:message (format "Unsupported media type. Currently only supports %s"
                               (str/join ", " supported-content-types))}]
                           true))
  :processable? (fn [{parsed ::parsed}] (includes_required? parsed))
  :post-redirect? false
  ;;:post-to-existing? false
  :respond-with-entity? true
  :post! (fn [{parsed ::parsed}]
           {::new
            (entity/discoverable-resource-create
             (:resource_name parsed)
             (:link_relation parsed)
             (:href parsed))})
  :handle-created (fn [{entity ::new}]
                    (json/generate-string
                        (entity/hypermedia-map entity))
                    ))
