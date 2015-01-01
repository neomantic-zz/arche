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

(ns arche.resources.profiles-spec
  (:use arche.resources.profiles)
  (:require [speclj.core :refer :all]
            [ring.util.response :only [:get-header] :as ring]
            [clojurewerkz.urly.core :as urly]
            [arche.core :refer [handler] :as web]
            [arche.app-state :as app]
            [arche.config :refer [base-uri]]
            [ring.mock.request :refer :all :as ring-mock]))

(defn make-mock-request [uri mime-type]
  (web/handler (header
                (ring-mock/request :get uri)
                "Accept" mime-type)))

(describe
 "GET alps/{profile} request"
 (it "should have the correct accept header"
     (should= "application/alps+json,application/json"
              (-> (make-mock-request "/alps/DiscoverableResources" "application/alps+json")
                  (ring/get-header "Accept"))))
 (it "should have the location header"
     (should= (.toString (.mutatePath (urly/url-like base-uri)  "/alps/DiscoverableResources"))
              (-> (make-mock-request "/alps/DiscoverableResources" "application/alps+json")
                  (ring/get-header "Location")))))

(let [response (make-mock-request "/alps/DiscoverableResources"  "application/x-yaml")
      actual-status (:status response)
      actual-body (:body response)]
  (describe
   "profile routes with failed accept type"
   (it "returns 406"
       (should= 406 actual-status))
   (it "returns the correct body"
       (should= "Unsupported media-type. Supported media type listed in Accept header." actual-body))
   (it "returns the correct accept type in the response header"
       (should= "application/alps+json,application/json" (ring/get-header response "Accept")))))
