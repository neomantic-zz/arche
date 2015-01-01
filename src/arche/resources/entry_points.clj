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

(ns arche.resources.entry-points
  (:require  [liberator.core :refer [resource defresource]]
             [arche.resources.discoverable-resource :only (discoverable-resources-all) :as record]
             [arche.media :as media]
             [arche.http :as http-helper]
             [arche.app-state :as app]
             [arche.config :refer [cache-expiry]]
             [arche.alps :as alps]
             [cheshire.core :refer :all :as json]
             [clojurewerkz.urly.core :as urly]
             [ring.util.codec :only [:url-encode] :as ring]
             [clojure.string :as str]
             [inflections.core :only (dasherize) :as inflect]
             [arche.resources.profiles :as profile]
             [liberator.representation :as rep :refer [ring-response as-response]]))

(def names
  "A hash-map of user friendly names of the EntryPoints resource"
  (let [base-name "entry_points"]
    {:titleized "EntryPoints"
     :alps-type base-name
     :keyword (keyword (inflect/dasherize base-name))}))

(def route "/")

(defn profile-url
  "Returns a string representation an alps entry point URL"
  []
  (app/alps-profile-url (:titleized names)))

(defn type-url
  "Returns a string of a type URL"
  []
  (format "%s#%s" (profile-url) (:alps-type names)))

(defn self-url
  "Returns a string of a self URL"
  []
  (.toString (.mutatePath
              (urly/url-like (app/base-uri))
              route)))

(defn entry-points-map
  "Returns a map suitable for JSON serialization of all entry points"
  []
  (let [discoverable-resources (record/discoverable-resources-all)]
    {media/keyword-links
     (apply conj
            (concat
             [(media/profile-link-relation (profile-url))
              (media/type-link-relation (type-url))
              (media/self-link-relation (self-url))]
             (map (fn [discoverable]
                    {(keyword (:resource_name discoverable))
                     {media/keyword-href (:href discoverable)}})
                  discoverable-resources)))}))

(defn alps-profile-map
  "Returns a profile map suitable for JSON serialization"
  []
  (let [all (record/discoverable-resources-all)
        entry-points-id "list"
        make-id (fn [id]
                  (str "#" id))
        base-alps-url (app/alps-profile-url (:titleized names))]
    (alps/document-hash-map
     (merge
      (alps/version "1.0")
      (alps/doc "Describes the semantics, states and state transitions associated with Entry Points.")
      {alps/keyword-link
       [{alps/keyword-href base-alps-url
         alps/keyword-rel (name media/link-relation-self)}]}
      (alps/descriptor
       (apply vector
        (concat
         (list
          (alps/descriptor-semantic
           (alps/id (:alps-type names))
           (alps/doc "A collection of link relations to find resources of a specific type")
           (alps/descriptor
            (apply vector
                   (concat
                    (list {alps/keyword-href (make-id entry-points-id)})
                    (map (fn [{name :resource_name}]
                           {alps/keyword-href (make-id name)}) all)))))
          (alps/descriptor-safe
           (alps/id entry-points-id)
           {alps/keyword-name "self"}
           (alps/rt (format "%s#%s"
                            base-alps-url
                            (ring/url-encode (:alps-type names))))
           (alps/doc "Returns a list of entry points")))
         (map (fn [{name :resource_name
                    link-relation-url :link_relation_url}]
                (alps/descriptor-safe
                 (alps/id name)
                 {alps/keyword-name name}
                 (alps/link :profile link-relation-url)
                 (alps/doc
                  (format "Returns a resource of the type '%s' as described by its profile"
                          name))
                 (alps/rt link-relation-url)))
              all))))))))

(defresource entry-points []
  :available-media-types [media/hal-media-type media/hale-media-type media/json-media-type]
  :allowed-methods [:get]
  :exists? (fn [_]
             [true {::body (json/generate-string (entry-points-map))}])
  :handle-ok (fn [{resource :resource, body ::body}]
               (ring-response
                {:status 200
                 :headers (into {}
                                [(http-helper/header-location (self-url))
                                 (http-helper/cache-control-header-private-age (cache-expiry))
                                 (http-helper/header-accept
                                  (str/join "," ((:available-media-types resource))))])
                 :body body}))
  :etag (fn [{body ::body}]
          (http-helper/etag-make body)))

(profile/profile-register!
 {(:keyword names) alps-profile-map})
