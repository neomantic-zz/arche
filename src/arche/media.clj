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

;; arche.media contains keywords and methods for creating hal and hale hypermediaa
;; documents

(def keyword-links :_links)
(def keyword-href :href)
(def hal-media-type "application/hal+json")
(def hale-media-type "application/vnd.hale+json")
(def json-media-type "application/json")
(def link-relation-self :self)
(def link-relation-profile :profile)
(def link-relation-type :type)
(def link-relation-next :next)
(def link-relation-prev :prev)
(def keyword-embedded :_embedded)
(def hale-keyword-type :type)
(def hale-type-text (hash-map hale-keyword-type "text:text"))
(def hale-keyword-method :method)
(def hale-keyword-data :data)

(defn- link-map-fn [link-relation-type]
  (fn [uri]
    {link-relation-type
     (hash-map keyword-href uri)}))

(def self-link-relation
  "Returns a hash-map representation of a HAL hypermedia link
  whose link-relation type is 'self', and
  whose href is set to the value of the parameter"
  (link-map-fn link-relation-self))

(def profile-link-relation
  "Returns a hash-map representation a HAL hypermedia link
  whose link-relation type is 'profile', and
  whose href is set to the value of the parameter"
  (link-map-fn link-relation-profile))

(def type-link-relation
  "Returns a hash-map representation a HAL hypermedia link
  whose link-relation type is 'type', and
  whose href is set to the value of the parameter"
  (link-map-fn link-relation-type))

(def prev-link-relation
  "Returns a hash-map representation a HAL hypermedia link
  whose link-relation type is 'prev', and
  whose href is set to the value of the parameter"
  (link-map-fn link-relation-prev))

(def next-link-relation
  "Returns a hash-map representation a HAL hypermedia link
  whose link-relation type is 'next', and
  whose href is set to the value of the parameter"
  (link-map-fn link-relation-next))
