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

(ns arche.media)

(def keyword-links :_links)
(def keyword-href :href)
(def hal-media-type "application/hal+json")
(def hale-media-type "application/vnd.hale+json")
(def json-media-type "application/json")
(def link-relation-self :self)
(def link-relation-profile :profile)
(def link-relation-type :type)
(def keyword-embedded :_embedded)
(def hale-keyword-type :type)
(def hale-type-text (hash-map hale-keyword-type "text:text"))
(def hale-keyword-method :method)
(def hale-keyword-data :data)

(defn- link-relation-value [link-relation-type]
  (fn [uri]
    {link-relation-type
     (hash-map keyword-href uri)}))

(def self-link-relation (link-relation-value link-relation-self))

(def profile-link-relation (link-relation-value link-relation-profile))

(def type-link-relation (link-relation-value link-relation-type))
