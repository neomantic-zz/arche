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

(ns arche.http
  (:require [pandect.core :refer [md5]]))

;; arche.http contains helps function for managing http headers

(defn- make-header-fn [key]
  (fn [content]
    {key content}))

(def header-location
  "Returns a hash-map with 'Location' as its key and the parameter set as its value"
  (make-header-fn "Location"))

(def header-accept
  "Returns a hash-map with 'Accept' as its key and the parameter set as its value"
  (make-header-fn "Accept"))

(def header-cache-control
  "Returns a hash-map with 'Cache-Control' as its key and the parameter set as its value"
  (make-header-fn "Cache-Control"))

(def header-etag
  "Returns a hash-map with 'Etag' as its key and the parameter set as its value"
  (make-header-fn "ETag"))

(def header-content-type
  "Returns a hash-map with 'Content-Type' as its key and the parameter set as its value"
  (make-header-fn "Content-Type"))

(defn cache-control-header-private-age [number]
  "Returns a hash-map with the key set as Cache-Control, and the value
   set to private and a max-age equal to the value of the parameter"
  (header-cache-control (format "max-age=%d, private" number)))

(defn etag-make [string]
  "Given a string, return a md5 hash intended to me used for an etag"
  (md5 string))
