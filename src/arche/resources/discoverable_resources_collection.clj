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
  (:require [liberator.core :refer [resource defresource =method]]
            [liberator.representation :refer [ring-response as-response]]
            [cheshire.core :refer :all :as json]
            [clojure.string :only (join) :as str]
            [clojure.java.io :as io]
            [arche.http :as http-helper]
            [arche.app-state :as app]
            [arche.validations :refer [default-error-messages]]
            [arche.resources.discoverable-resource
             :refer :all :as entity :exclude [hypermedia-map]]
            [arche.resources.core :refer :all :as generic]
            [arche.media :as media]))

(defn- method-supports-body? [ctx]
  (#{:put :post} (get-in ctx [:request :request-method])))

(def supported-content-types ["application/json"])

(def error-messages
  (assoc default-error-messages
    :taken-by "has already been taken"))

(defn respond-to-unprocessable [{errors ::errors}]
  (ring-response
   {:status 422
    :headers (conj
              (http-helper/cache-control-header-private-age 0)
              (http-helper/header-content-type "application/json"))
    :body (json/generate-string
               (generic/construct-error-map errors error-messages))}))

(defn- supported-content-type? [liberator-ctx]
  (some #{(get-in liberator-ctx [:request :headers "content-type"])} supported-content-types))

(defn- index-action? [ctx]
  (=method :get ctx))

(defn- create-action? [ctx]
  (=method :post ctx))

(def self-url
  (app/app-uri-for (str "/" (:routable entity/names))))

(def type-url
  (format "%s#%s" entity/profile-url (:routable entity/names)))

(defn hypermedia-map [records]
  {:items (apply vector
                 (map
                  (fn [record]
                    {media/keyword-href (entity/url-for record)})
                  records))
   media/keyword-embedded {:items
                           (apply vector
                                  (map (fn [{:keys [link_relation_url href resource_name] :as record}]
                                   {:link_relation_url link_relation_url
                                    :href href
                                    :resource_name resource_name
                                    media/keyword-links (media/self-link-relation (entity/url-for record))
                                    })
                                 records))}
   media/keyword-links
   (media/self-link-relation self-url)})

(defresource discoverable-resources-collection [request]
  :allowed-methods [:post :get]
  :available-media-types [media/hal-media-type]
  :handle-not-acceptable generic/not-acceptable-response
  :malformed? (fn [ctx]
                (if (method-supports-body? ctx)
                  (try
                    (if-let [body (get-in ctx [:request :body])]
                      [false {::parsed
                              (json/parse-string
                               (condp instance? body
                                 java.lang.String body
                                 (slurp (io/reader body))) true)}]
                      [true {:message "No Body"}])
                    (catch Exception e
                      [true {:message "Required valid content for Content-Type applicaton/json"}]))
                  false))
  :known-content-type? (fn [ctx]
                         (if (index-action? ctx)
                           true
                             (if (and (create-action? ctx)
                                      (supported-content-type? ctx))
                               true
                               [false {:message (format "Unsupported media type. Currently only supports %s"
                                                        (str/join ", " supported-content-types))}])))
  :processable? (fn [{parsed ::parsed, :as ctx}]
                  (if (index-action? ctx)
                    true
                    (let [errors (entity/validate parsed)]
                      (if (empty? errors)
                        true
                        [false {::errors errors}]))))
  :handle-unprocessable-entity respond-to-unprocessable
  :post-redirect? false
  :respond-with-entity? true
  :exists? (fn [ctx]
             (if (index-action? ctx)
               [true {::body (json/generate-string
                              (hypermedia-map (discoverable-resources-all)))}]
               false))
  :handle-ok (fn [{body ::body}]
               (ring-response
                {:status 200
                 :headers (into {}
                                [(http-helper/cache-control-header-private-age 0)
                                 (http-helper/header-location self-url)
                                 (http-helper/header-accept media/hal-media-type)])
                 :body body}))
  :post! (fn [{parsed ::parsed}]
           {::record
            (entity/discoverable-resource-create
             (:resource_name parsed)
             (:link_relation_url parsed)
             (:href parsed))})
  :handle-created (fn [{record ::record}]
                    (entity/ring-response-json record 201))
  :etag (fn [ctx]
          (cond
           (index-action? ctx) (http-helper/etag-make (::body ctx))
           (create-action? ctx) (if-let [record (::record ctx)]
                                  (entity/etag-for (::record ctx)))
           :else nil)))
