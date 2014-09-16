(ns wormhole-clj.app-state
  (:require [environ.core :refer [env]]
            [wormhole-clj.alps :only [keyword-alps] :as alps]
            [ring.util.codec :only [url-encode] :as ring]
            [clojurewerkz.urly.core :as urly])
  (:import [java.net URI URL]))

(defn cache-expiry []
  (if-let [expiry (env :cache-expiry)]
    expiry
    600))

(defn base-uri []
  (if-let [uri (env :base-uri)]
    uri
    (throw (Exception. "Missing base uri environmental variable"))))

(defn app-uri-for [path]
  (let [url (urly/url-like (base-uri))]
    (.toString (.mutatePath url path))))

(def alps-path (name alps/keyword-alps))

(defn alps-profile-url [resource-name]
  (app-uri-for (format "%s/%s" alps-path resource-name)))
