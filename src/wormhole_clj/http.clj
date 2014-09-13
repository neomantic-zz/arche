(ns wormhole-clj.http
  (:require [pandect.core :refer :all :as digest]))

(defn- make-header-fn [key]
  (fn [content]
    {key content}))

(def header-location (make-header-fn "Location"))
(def header-accept (make-header-fn "Accept"))
(def header-cache-control (make-header-fn "Cache-Control"))
(def header-etag (make-header-fn "ETag"))

(defn cache-control-header-private-age [number]
  (header-cache-control (format "max-age=%d, private" number)))

(defn body-etag-make [body]
  (digest/md5 body))
