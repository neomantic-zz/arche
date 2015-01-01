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
  (:require [arche.alps :only [keyword-alps] :as alps]
            [arche.config :refer [base-uri]]
            [ring.util.codec :only [url-encode] :as ring]
            [clojurewerkz.urly.core :as urly])
  (:import [java.net URI URL]))

(defn app-uri-for
  "Given a path, this function returns a string representation of the path
  with the base-uri prepended to it."
  [path]
  (let [url (urly/url-like base-uri)]
    (.toString (.mutatePath url path))))

(def alps-path
  "The default name of the alps path: '/alps'"
  (name alps/keyword-alps))

(defn alps-profile-url
  "Given a resource name, returns the an alps profile URL for that
  resource. E.g. 'discoverable_resources => http://example.org/alps/discoverable_resources"
  [resource-name]
  (app-uri-for (format "%s/%s" alps-path resource-name)))
