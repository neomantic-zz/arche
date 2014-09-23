(ns wormhole-clj.alps-spec
  (:use wormhole-clj.alps)
  (:require [speclj.core :refer :all]))


(describe
 "media types"
 (it "returns the correct value for json media type"
     (should= "application/alps+json" json-media-type)))

(describe
 "document"
 (it "returns the correct version"
     (should= "1.0" document-version)))

(describe
 "alps reserved key words"
 (it "returns the correct keyword for alps"
     (should= :alps keyword-alps))
 (it "returns the correct keyword for href"
     (should= :href keyword-href))
 (it "returns the correct keyword for type"
     (should= :type keyword-type))
 (it "returns the correct keyword for doc"
     (should= :doc keyword-doc))
 (it "returns the correct keyword for id"
     (should= :id keyword-id))
 (it "returns the correct keyword for value"
     (should= :value keyword-value))
 (it "returns the correct keyword for link"
     (should= :link keyword-link))
 (it "returns the correct keyword for rt"
     (should= :rt keyword-rt))
 (it "returns the correct keyword for rel"
     (should= :rel keyword-rel))
 (it "returns the correct keyword for version"
     (should= :version keyword-version)))

(describe
 "alps reserved values"
 (it "returns correct string for semantic"
     (should= "semantic" (:semantic descriptor-types)))
 (it "returns correct string for semantic"
     (should= "safe" (:safe descriptor-types))))

(describe
 "making descriptors"
 (it "returns the correct map for a safe descriptor"
     (should== {:type "safe"
                :id "g"
                :rel "q"}
               (descriptor-safe
                (id "g")
                (rel "q"))))
 (it "returns the correct map for a semantic descriptor"
     (should== {:type "semantic"
                :id "g"
                :rel "q"}
               (descriptor-semantic
                (id "g")
                (rel "q")))))

(describe
 "building descriptor elements"
 (it "returns correct map for id"
     (should== {:id "g"}
               (id "g")))
 (it "returns correct map for alps"
     (should== {:alps "g"}
               (alps "g")))
 (it "returns correct map for href"
     (should== {:href "g"}
               (href "g")))
 (it "returns correct map for type"
     (should== {:type "g"}
               (type "g")))
 (it "returns correct map for rt"
     (should== {:rt "g"}
               (rt "g")))
 (it "returns correct map for rel"
     (should== {:rel "g"}
               (rel "g")))
 (it "returns correct map for doc"
     (should== {:doc {:value "documentation"}}
               (doc "documentation")))
 (it "returns correct link map"
     (should== {:link {:rel "self" :href "url"}}
               (link :self "url")))
 (it "returns the correct descriptor map"
     (should== {:descriptor {}}
               (descriptor {})))
 (it "returns the correct version map"
     (should== {:version {}}
               (version {}))))

(describe
 "alps schemas"
 (it "returns correct value for the URL schema"
     (should= "http://alps.io/schema.org/URL" (:url schemas)))
 (it "returns correct value for the Text schema"
     (should= "http://alps.io/schema.org/Text" (:text schemas))))
