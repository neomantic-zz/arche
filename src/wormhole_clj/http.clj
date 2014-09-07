(ns wormhole-clj.http)

(defn- make-header-fn [key]
  (fn [content]
    {key content}))

(def header-location (make-header-fn "Location"))
(def header-accept (make-header-fn "Accept"))
(def header-cache-control (make-header-fn "Cache-Control"))

(defn cache-control-header-private-age [number]
  (header-cache-control (format "max-age=%d, private" number)))
