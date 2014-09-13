(ns wormhole-clj.alps)

(def json-media-type "application/alps+json")

;; TODO Use a macro
(def keyword-descriptor :descriptor)
(def keyword-alps  :alps)
(def keyword-href  :href)
(def keyword-type  :type)
(def keyword-doc   :doc)
(def keyword-id    :id)
(def keyword-value :value)
(def keyword-link  :link)
(def keyword-rt    :rt)
(def keyword-rel    :rel)

(def type-value-semantic "semantic")
(def type-value-safe "safe")

(def schema-url "http://alps.io/schema.org/URL")
(def schema-text "http://alps.io/schema.org/Text")

(defn document-hash-map [elements]
  {keyword-alps elements})