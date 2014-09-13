(ns wormhole-clj.media)

(def keyword-links :_links)
(def keyword-href :href)
(def hale-media-type "application/vnd.hale+json")
(def link-relation-self :self)

(defn link-relation-value [link-relation-type uri]
  {link-relation-type
   (hash-map keyword-href uri)})

(defn self-link-relation [uri]
  (link-relation-value link-relation-self uri))