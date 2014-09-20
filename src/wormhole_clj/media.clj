(ns wormhole-clj.media)

(def keyword-links :_links)
(def keyword-href :href)
(def hal-media-type "application/hal+json")
(def link-relation-self :self)
(def link-relation-profile :profile)
(def link-relation-type :type)

(defn- link-relation-value [link-relation-type]
  (fn [uri]
    {link-relation-type
     (hash-map keyword-href uri)}))

(def self-link-relation (link-relation-value link-relation-self))

(def profile-link-relation (link-relation-value link-relation-profile))

(def type-link-relation (link-relation-value link-relation-type))
