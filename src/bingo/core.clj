(ns bingo.core
  (:require [org.httpkit.client :as http]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))

(def bing-url "http://www.bing.com")


(def cli-options
  [["-o" "--output-dir DIRECTORY" "Output Directory"
    :default "./output"
    ]])


(defn create-file
  "doc-string"
  [content filename output-dir]
  (let [output-file (str output-dir "/" filename)]
    (io/make-parents output-file)
    (io/copy content (java.io.File. output-file))))


(defn download-image
  "Download image from url in output-dir"
  [url filename out-dir]
  (let [{:keys [status headers body error] :as resp} @(http/get url {:as :stream})]
    (if error
      (println "Failed, error is " error)
      (create-file body filename out-dir))))


(defn get-metadata
  "doc-string"
  [json]
  (let [url (get-in json ["images" 0 "url"])
        metadata {}]
    (assoc metadata :url (str bing-url url)
           :filename (str (nth (re-find #"(.*OHR\.)(.*)(_EN-US.*)" url) 2) ".jpeg"))))

(defn get-image-metadata
  "Get Image"
  [& args]
  (let [{:keys [status headers body error] :as resp} @(http/get "http://www.bing.com/HPImageArchive.aspx?&format=js&idx=0&mkt=en-US&n=1")]
    (if error
      (println "Failed, error is " error)
      (get-metadata (json/read-str body)))))


(defn -main
  "Download Bing's image wallpaper."
  [& args]
  (let [{:keys [output-dir]} (:options (parse-opts args cli-options))
        {:keys [url filename] } (get-image-metadata)]
    (println (format "Downloading %s to %s directory" filename output-dir))
    (download-image url filename output-dir)))
