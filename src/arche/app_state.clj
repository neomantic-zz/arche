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

(ns arche.app-state
  (:require [environ.core :refer [env]]
            [arche.alps :only [keyword-alps] :as alps]
            [ring.util.codec :only [url-encode] :as ring]
            [clojurewerkz.urly.core :as urly])
  (:import [java.net URI URL]))

(defn cache-expiry []
  "Returns cache-expiry to be returned in responses. If the CACHE_EXPIRY
  environmental variable is set, then this function returns that value.
  Otherwise, it defaults to 600 milliseconds."
  (if-let [expiry (env :cache-expiry)]
    expiry
    600))

(defn base-uri []
  "Returns the base uri of the application based on the required BASE_URI
  environmental variable.  An example of a base URI is http://www.example.org.
  All URLs that the app produces are based off this base-uri."
  (if-let [uri (env :base-uri)]
    uri
    (throw (Exception. "Missing base uri environmental variable"))))

(defn app-uri-for [path]
  "Given a path, this function returns a string representation of the path
  with the base-uri prepended to it."
  (let [url (urly/url-like (base-uri))]
    (.toString (.mutatePath url path))))

(def alps-path
  "The default name of the alps path: '/alps'"
  (name alps/keyword-alps))

(defn alps-profile-url [resource-name]
  "Given a resource name, returns the an alps profile URL for that
  resource. E.g. 'discoverable_resources => http://example.org/alps/discoverable_resources"
  (app-uri-for (format "%s/%s" alps-path resource-name)))
