(ns wormhole-clj.alps)

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

(defn alps-element [keyword]
  (fn [value] (hash-map keyword value)))

(def descriptor (alps-element keyword-descriptor))
(def alps (alps-element keyword-alps))
(def href (alps-element keyword-href))
(def type (alps-element keyword-type))
(def id (alps-element keyword-id))
(def rt (alps-element keyword-rt))
(def rel (alps-element keyword-rel))

(defn doc [documentation]
  {keyword-doc
    {keyword-value documentation}})

(defn link [link-relation url]
  {keyword-link
   (conj (rel link-relation)
         (href url))})

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
