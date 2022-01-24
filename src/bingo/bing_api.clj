(ns bingo.bing-api
  (:require
   [clojure.data.json :as data.json]
   [clojure.walk :as walk]
   [org.httpkit.client :as http]))

(def host "http://www.bing.com")

(def images-api-url (str host "/HPImageArchive.aspx"))

(defn fetch-images
  "Fetch N images for MARKET"
  [market nb-images]
  (-> {:url images-api-url
       :method :get
       :query-params {:format "js"
                      :idx 0
                      :mkt market
                      :n nb-images}}
      http/request
      deref
      :body
      data.json/read-str
      walk/keywordize-keys
      :images))

(defn download-image
  "Download image from URL"
  [url]
  (-> {:url (str host url)
       :method :get
       :options :stream}
      http/request
      deref
      :body))

