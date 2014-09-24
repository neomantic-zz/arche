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

(ns arche.media-spec
  (:require [speclj.core :refer :all]
            [arche.media :refer :all]))

(describe
 "keywords"
 (it "returns the correct keyword for links"
     (should= :_links keyword-links))
 (it "returns the correct keyword for href"
     (should= :href keyword-href))
 (it "returns the correct keyword for self"
     (should= :self link-relation-self))
 (it "returns the correct keyword for type"
     (should= :type link-relation-type)))

(describe
 "media types"
 (it "returns the correct string for hale+json"
     (should= "application/hal+json" hal-media-type)))

(describe
 "link relation"
 (it "returns map for self link relation value"
     (should= {:self {:href "uri"}}
             (self-link-relation "uri")))
 (it "returns map for profile link relation value"
     (should= {:profile {:href "uri"}}
              (profile-link-relation "uri")))
 (it "returns map for profile link relation value"
     (should= {:type {:href "uri"}}
              (type-link-relation "uri"))))
