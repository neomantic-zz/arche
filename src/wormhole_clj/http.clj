(ns wormhole-clj.http)

(defn- make-header-fn [key]
  (fn [content]
    {key content}))

(def header-location (make-header-fn "Location"))
(def header-accept (make-header-fn "Accept"))
