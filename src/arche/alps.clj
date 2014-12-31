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

;; The identifiers and functions in arche.alps are to be used for creating
;; alps profile documents


(def document-version
  "Alps version number"
  "1.0")

(def json-media-type
  "Alps JSON mime-type"
  "application/alps+json")

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

(defn- alps-element-fn
  "Given an alps reserved word, returns a function that
  accepts 1 param, the value, to associate with the keyword
  when the function returns a hash-map"
  [keyword]
  (fn [value] (hash-map keyword value)))

(def alps
  "A function that associates a alue with the alps reserved word"
  (alps-element-fn keyword-alps))

(def href
  "A function that associates a value with the alps href reserved word"
  (alps-element-fn keyword-href))

(def type
  "A function that associates a value with the alps type reserved word"
  (alps-element-fn keyword-type))

(def id
  "A function that associates a value with the alps id reserved word"
  (alps-element-fn keyword-id))

(def rt
  "A function that associates a value with the alps rt (return type) reserved word"
  (alps-element-fn keyword-rt))

(def rel
  "A function that associates a value with the alps rel reserved word"
  (alps-element-fn keyword-rel))

(def version
  "A function that associates a value with the alps version reserved word"
  (alps-element-fn keyword-version))

(defn doc [documentation]
  "Accepting a value representation documentation, this function associates it with the alps doc keyword"
  {keyword-doc
    {keyword-value documentation}})

(defn link [link-relation url]
  "Accepting both a link relation type and an URL, return a hash-map
  that represents an alps document's representation of a link"
  {keyword-link
   (conj (rel (name link-relation))
         (href url))})

(defn descriptor [descriptions]
  "Given a collection of descriptors, return a hash-map that represents
   an alps set of descriptors"
  ((alps-element-fn keyword-descriptor)
   descriptions))

(def descriptor-types
  "A hash map which associate a keyword with an alps string
  that represents the two kinds of descriptors: safe and semantic"
  {:safe "safe"
   :semantic "semantic"})

(def schemas
  "Returns a hash-map associating a keyword for two alps profiles: Text and URL"
  {:url "http://alps.io/schema.org/URL"
   :text "http://alps.io/schema.org/Text"})

(defn document-hash-map [elements]
  "Returns a hash-map representing the 'root' of a alps document"
  {keyword-alps elements})

(defn- descriptor-fn [descriptor-type]
  "Given a descriptor type, returns a function accepting option
   descriptor hash-maps that marks them as the given descriptor type"
  (fn [& descriptors]
    (merge {keyword-type (get descriptor-types descriptor-type)}
           (into {} descriptors))))

(def descriptor-safe
  "Given a set of descriptors, returns a hash-map
   describing a 'safe' descriptor."
  (descriptor-fn :safe))

(def descriptor-semantic
  "Given a set of descriptors, returns a hash-map
   describing a 'semantic' descriptor."
  (descriptor-fn :semantic))
