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

(ns arche.alps
  (:refer-clojure :exclude [resolve type]))

(def document-version "1.0")

(def json-media-type "application/alps+json")

(def keyword-doc        :doc)
(def keyword-value      :value)
(def keyword-descriptor :descriptor)
(def keyword-alps       :alps)
(def keyword-href       :href)
(def keyword-type       :type)
(def keyword-id         :id)
(def keyword-rt         :rt)
(def keyword-rel        :rel)
(def keyword-link       :link)
(def keyword-version    :version)
(def keyword-name       :name)

(defn alps-element [keyword]
  (fn [value] (hash-map keyword value)))

(def alps (alps-element keyword-alps))
(def href (alps-element keyword-href))
(def type (alps-element keyword-type))
(def id (alps-element keyword-id))
(def rt (alps-element keyword-rt))
(def rel (alps-element keyword-rel))
(def version (alps-element keyword-version))

(defn doc [documentation]
  {keyword-doc
    {keyword-value documentation}})

(defn link [link-relation url]
  {keyword-link
   (conj (rel (name link-relation))
         (href url))})

(defn descriptor [descriptions]
  ((alps-element keyword-descriptor)
   descriptions))

(def descriptor-types
  {:safe "safe"
   :semantic "semantic"})

(def schemas
  {:url "http://alps.io/schema.org/URL"
   :text "http://alps.io/schema.org/Text"})

(defn document-hash-map [elements]
  {keyword-alps elements})

(defn descriptor-make [type]
  (fn [& descriptors]
    (merge {keyword-type ((keyword type) descriptor-types)}
           (into {} descriptors))))

(def descriptor-safe (descriptor-make "safe"))
(def descriptor-semantic (descriptor-make "semantic"))
