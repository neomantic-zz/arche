(ns wormhole-clj.alps
  (:refer-clojure :exclude [type]))

(def json-media-type "application/alps+json")

(defmacro defalps [alps-element]
  `(do
     (def ~(symbol (format "keyword-%s" alps-element))
       (keyword '~alps-element))
     (defn ~alps-element [~'value ~'descriptor]
      (conj
       ~'descriptor
       (hash-map (keyword '~alps-element) ~'value)))))

(def keyword-doc   :doc)
(def keyword-value :value)

(defalps descriptor)
(defalps alps)
(defalps href)
(defalps type)
(defalps id)
(defalps rt)
(defalps link)
(defalps rel)

(defn doc [documentation descriptor]
  (conj
   descriptor
   {keyword-doc
    {keyword-value documentation}}))

(def types
  {:safe "safe"
   :semantic "semantic"})

(def schemas
  {:url "http://alps.io/schema.org/URL"
   :text "http://alps.io/schema.org/Text"})

(defn document-hash-map [elements]
  {keyword-alps elements})

(defn- descriptor-type [type-key]
  (fn []
    (type (type-key types) {})))

(def descriptor-type-safe (descriptor-type :safe))
(def descriptor-type-semantic (descriptor-type :semantic))
