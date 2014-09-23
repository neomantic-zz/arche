(ns wormhole-clj.resources.entry-points
  (:require  [liberator.core :refer [resource defresource]]
             [wormhole-clj.resources.discoverable-resources :only (discoverable-resources-all) :as record]
             [wormhole-clj.media :as media]
             [wormhole-clj.http :as http-helper]
             [wormhole-clj.app-state :as app]
             [wormhole-clj.alps :as alps]
             [cheshire.core :refer :all :as json]
             [clojurewerkz.urly.core :as urly]
             [ring.util.codec :only [:url-encode] :as ring]
             [clojure.string :as str]
             [inflections.core :only (dasherize) :as inflect]
             [wormhole-clj.resources.profiles :as profile]
             [liberator.representation :as rep :refer [ring-response as-response]]))

(def names
  (let [base-name "entry_points"]
    {:titleized "EntryPoints"
     :alps-type base-name
     :keyword (keyword (inflect/dasherize base-name))}))

(def route "/")

(defn profile-url []
  (app/alps-profile-url (:titleized names)))

(defn type-url []
  (format "%s#%s" (profile-url) (:alps-type names)))

(defn self-url []
  (.toString (.mutatePath
              (urly/url-like (app/base-uri))
              route)))

(defn entry-points-map []
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

(defn alps-profile-map []
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
                    link-relation :link_relation}]
                (alps/descriptor-safe
                 (alps/id name)
                 {alps/keyword-name name}
                 (alps/link :profile link-relation)
                 (alps/doc
                  (format "Returns a resource of the type '%s' as described by its profile"
                          name))
                 (alps/rt link-relation)))
              all))))))))

(defresource entry-points []
  :available-media-types [media/hal-media-type]
  :allowed-methods [:get]
  :handle-ok (fn [{resource :resource}]
               (let [body (json/generate-string (entry-points-map))]
                 (ring-response
                  {:status 200
                   :headers (into {}
                                  [(http-helper/header-etag (http-helper/body-etag-make body))
                                   (http-helper/header-location (self-url))
                                   (http-helper/cache-control-header-private-age (app/cache-expiry))
                                   (http-helper/header-accept
                                    (str/join ", " ((:available-media-types resource))))])
                   :body body}))))

(profile/profile-register!
 {(:keyword names) alps-profile-map})
