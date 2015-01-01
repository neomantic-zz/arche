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
  (:use korma.core)
  (:require [liberator.core :refer [resource defresource =method]]
            [liberator.representation :refer [ring-response Representation]]
            [cheshire.core :refer :all :as json]
            [ring.middleware.params :refer :all]
            [clojure.string :only (join) :as str]
            [clojure.java.io :as io]
            [arche.http :as http-helper]
            [arche.config :refer [cache-expiry]]
            [arche.app-state :as app]
            [arche.paginate :refer :all]
            [arche.validations :refer [default-error-messages validate has-errors? errors-get]]
            [arche.resources.discoverable-resource
             :refer :all :as entity :exclude [hypermedia-map]]
            [arche.resources.core :refer :all :as common]
            [arche.media :as media]))

(defn- method-supports-body?
  "Returns true of our resource supports HTTP method that have a body.
  Returns true on POST and PUT only."
  [ctx]
  (#{:put :post} (get-in ctx [:request :request-method])))

(def supported-content-types
  "The support content type for a POST request."
  ["application/json"])

(def error-messages
  "The errors messages suitable for a discoverable resources POST request."
  (assoc default-error-messages
    :taken-by "has already been taken"))

(defn- requested-mime-type
  "Given a liberator context, returns the media-type"
  [liberator-ctx]
  (get-in liberator-ctx [:representation :media-type]))

(defn test-processable
  "Tests if the POST request is processable...if it contains all the
  required fields."
  [attributes]
  (validate attributes
            [entity/validate-href
             entity/validate-link-relation
             entity/validate-resource-name-present]))

(defn respond-with-errors
  "Given a status code and a hash-map of errors, returns a JSON body
  as a ring-response."
  [status errors]
  (ring-response
   {:status status
    :headers (conj
              (http-helper/cache-control-header-private-age 0)
              (http-helper/header-content-type "application/json"))
    :body (json/generate-string
           (common/error-map-make (errors-get errors) error-messages))}))

(defn respond-to-bad-request
  "Given a hash-map with the ::errors key, returns a ring response with
  the errors and the 400 status code."
  [{errors ::errors}]
  (respond-with-errors 400 errors))

(defn respond-to-unprocessable
  "Give a hash-map with the ::errors key, returns a ring response with the
  errors and the 422 error code"
  [{errors ::errors}]
  (respond-with-errors 422 errors))

(defn- supported-content-type?
  "Returns a boolean indicated if the submitted content type is supported"
  [liberator-ctx]
  (some #{(get-in liberator-ctx [:request :headers "content-type"])} supported-content-types))

(defn- index-action?
  "Returns a boolean indicating if the request is intended to retrieves
  the collection."
  [ctx]
  (=method :get ctx))

(defn- create-action?
  "Returns a boolean indicating if the request is intended to create an item"
  [ctx]
  (=method :post ctx))

(def self-url
  "Returns a string representing the URL for the collection of resources"
  (app/app-uri-for (str "/" (:routable entity/names))))

(def ^:private create-url self-url)

(def type-url
  "Returns a string for the collection type url used in the hypermedia document"
  (format "%s#%s" entity/profile-url (:routable entity/names)))

(def ^:private page-query-key "page")
(def ^:private per-page-query-key "per_page")

(def ^:private pagination-url-format
  "The format to be used when creating a paginatoin link"
  (str "%s?" page-query-key "=%d&" per-page-query-key "=%d"))

(defn paginated-url-fn
  "Given a user and a pagination page key (:prev-page, :next-page)
  return a function that creates the url"
  [url page-key]
  (fn [paginated]
    (format pagination-url-format
            url
            (get paginated page-key)
            (get paginated :per-page))))

(def prev-url
  "Returns a string for the pagination prev URL"
  (paginated-url-fn self-url :prev-page))

(def next-url
  "Returns a string for the pagination next URL"
  (paginated-url-fn self-url :next-page))

(defn- self-link
  "Returns the self link string with optional pagination params"
  [paginated]
  (media/self-link-relation
   (let [page (:page paginated)]
     (if (> page 0)
       (format pagination-url-format
               self-url
               page
               (:per-page paginated))
       self-url))))

(defn- with-pagination-predicates [paginated prev-next-fn]
  (let [has-next (next-page-key paginated)
        has-prev (prev-page-key paginated)]
    (prev-next-fn has-prev has-next)))

(defn hal-links
  "Returns a hash map with hal links used for a discoverable resources resource"
  [paginated]
  (with-pagination-predicates paginated
    (fn [has-prev has-next]
      (let [self-link  (self-link paginated)]
        (cond
          (and has-next has-prev) (apply
                                   conj
                                   [self-link
                                    (media/prev-link-relation (prev-url paginated))
                                    (media/next-link-relation (next-url paginated))])
          has-next (conj self-link (media/next-link-relation (next-url paginated)))
          has-prev (conj self-link (media/prev-link-relation (prev-url paginated)))
          :else self-link)))))

(defn hal-map
  "Given a hash-map with the :records keyword, return a hashmap suitable for
  JSON serialization to a HAL document"
  [paginated]
  (let [records (:records paginated)]
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
     (hal-links paginated)}))

(defn hale-map
  "Returns a hash-map suitable for a HALE JSON serialization"
  [paginated]
  (let [hal-map (hal-map paginated)
        links (media/keyword-links hal-map)]
    ;;there is nothing "smart" about this map...i.e., inspecting
    ;; the resource, and find the route, expected-attributes, etc
    (assoc
     hal-map
      media/keyword-links
      (conj
       links
       {:create (hash-map media/hale-keyword-method "POST",
                          media/keyword-href create-url,
                          media/hale-keyword-data (into {} (map #(hash-map % media/hale-type-text) required-descriptors)))}))))

(defn- index-ring-map
  "Given a liberator context and a hash-map representation the response, return
  a hash-map suitable for a ring response"
  [context hypermedia-map]
  (let [json (json/generate-string hypermedia-map)]
    {:body json
     :status 200
     :headers (into {}
                    [(-> json
                         http-helper/etag-make
                         http-helper/header-etag)
                     (http-helper/cache-control-header-private-age (cache-expiry))
                     (http-helper/header-location self-url)
                     (http-helper/header-accept
                      (str/join "," ((:available-media-types (:resource context)))))])}))

(defn- create-header-links
  "Returns the string used in a Link header that indicates response pagination."
  [paginated]
  (clojure.string/join
   ", "
   (with-pagination-predicates paginated
     (fn [has-next has-prev]
       (defn format-url-fn [url-fn link-relation-type]
         (fn []
           (format "<%s>; rel=\"%s\"" (url-fn paginated) link-relation-type)))
       (def prev-link-url (format-url-fn prev-url "previous"))
       (def next-link-url (format-url-fn next-url (name media/link-relation-next)))
       (cond
         (and has-next has-prev) [(prev-link-url)
                                  (next-link-url)]
         has-next [(next-link-url)]
         has-prev [(prev-link-url)]
         :else [])))))

(defn- index-response-fn
  [map-fn]
  (fn [context paginated]
    (let [ring-hashmap (index-ring-map context (map-fn paginated))
          given-headers (:headers ring-hashmap)
          link-header (create-header-links paginated)]
      (if (empty? link-header)
        ring-hashmap
        (assoc
            ring-hashmap
            :headers
            (assoc given-headers
               "Link" link-header))))))

(defrecord ^:private HalResponse
  [paginated]
  Representation
  (as-response [this context]
    ((index-response-fn hal-map) context paginated)))

(defrecord ^:private HaleResponse [paginated]
  Representation
  (as-response [this context]
    ((index-response-fn hale-map) context paginated)))

(def default-per-page
  "The default per page pagination count"
  25)

(def discoverable-resources-paginate
  "Returns a hash-map representation a paginated collection of
  discoverable resources"
  (paginate-fn
   (fn [start number-of-items]
     (select discoverable-resources
             (offset start)
             (limit number-of-items)
             (order :id :ASC)))
   default-per-page))

(defn query-params->pagination-params [query-params]
  "Converts a HTTP requests query params to those suitable for the
  pagination fn."
  (defn param->integer [param-value]
    (if (nil? param-value) param-value
        (try
          (Integer. param-value)
          (catch Exception e
            nil))))
  (let [page-query (get query-params page-query-key)
        per-page-query (get query-params per-page-query-key)]
    (vector (let [pagination-page (param->integer page-query)]
              (if (or (nil? pagination-page) (<= pagination-page 0))
                1
                pagination-page))
            (let [pagination-per-page (param->integer per-page-query)]
              (cond
                (and (not (empty? per-page-query))
                     (nil? pagination-per-page)) 0
                (or (nil? pagination-per-page)
                   (> pagination-per-page default-per-page)) default-per-page
                (<= pagination-per-page 0) 0
                :else pagination-per-page)))))

;; A liberator resource representing resource called DiscoverableResources; a
;; collection resource that of individual discoverable resource
(defresource discoverable-resources-collection
  [request]
  :allowed-methods [:post :get]
  :available-media-types [media/hale-media-type media/hal-media-type]
  :handle-not-acceptable common/not-acceptable-response
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
                  (if (create-action? ctx)
                    (let [test (test-processable parsed)]
                      (if (has-errors? test)
                        [false {::errors test}]
                        true))
                    true))
  :handle-unprocessable-entity respond-to-unprocessable
  :post-redirect? false
  :respond-with-entity? true
  :exists? (fn [ctx] (index-action? ctx))
  :handle-ok (fn [ctx]
               (let [records (apply discoverable-resources-paginate
                                    (-> ctx :request :query-params query-params->pagination-params))]
                 (if (= (requested-mime-type ctx) media/hale-media-type)
                   (HaleResponse. records)
                   (HalResponse. records))))
  :post! (fn [{parsed ::parsed}]
           (let [errors (validate-uniqueness parsed)]
             (if (has-errors? errors)
               {::errors errors}
               {::record (entity/discoverable-resource-create
                          {:resource-name (:resource_name parsed)
                           :link-relation-url (:link_relation_url parsed)
                           :href (:href parsed)})})))
  :handle-created (fn [{record ::record, errors ::errors}]
                    (if (not-empty errors)
                      (respond-with-errors 400 errors)
                      (entity/ring-response-json record 201))))
