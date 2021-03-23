(ns bingo.core
  (:require [org.httpkit.client :as http]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as str])
  (:gen-class))

(def bing-url "http://www.bing.com")


(def cli-options
  [["-o" "--output-dir DIRECTORY" "Output Directory"
    :default "./output"]
   ["-n" "--nb-images IMAGES" "Numbers of images (max 7)"
    :default 1]
   ["-h" "--help"]])


(defn create-file
  "Create Image File"
  [content filename output-dir]
  (let [output-file (str output-dir "/" filename)]
    (io/make-parents output-file)
    (io/copy content (java.io.File. output-file))
    output-file))


(defn download-image
  "Download image from url in output-dir"
  [out-dir {:keys [url filename]}]
  (let [{:keys [status headers body error] :as resp} @(http/get url {:as :stream})]
    (if error
      (println (format "Failed, error is " error))
      (create-file body filename out-dir))))


(defn create-image
  "Create Image"
  [uri]
  (assoc {} :url (str bing-url uri)
            :filename (str (nth (re-find #"(.*OHR\.)(.*)(_FR-FR.*)" uri) 2) ".jpeg")))

(defn parse-json
  "Parse JSON"
  [json]
  (map #(create-image (get-in % ["url"])) (get-in  json ["images"])))

(defn http-get
  "Get image metadata as JSON"
  [url]
  (let [{:keys [status headers body error] :as resp} @(http/get url)]
    (cond error (println "Request failed " error)
          (not (= status 200)) (println "Request failed with status " status)
          :else (json/read-str body))))

(defn get-images
  "Returns availables images"
  [nb-images]
  (some->> nb-images
     (format "http://www.bing.com/HPImageArchive.aspx?&format=js&idx=0&mkt=fr-FR&n=%s")
     (http-get)
     (parse-json)))


(defn usage [options-summary]
  (->> ["Bingo : a CLI to get Bing's daily walpapers."
        ""
        "Usage: bingo [options]"
        ""
        "Options:"
        options-summary
        ""
        "Examples of usage :"
        "  bingo                Download bing walpaper of the day in ./output directory"
        "  bingo -n 7 -o /tmp   Downlaod last 7 bing walpapers in /tmp directory"
        ""]
       (str/join \newline)))

(defn- exit [msg]
  (println msg)
  (System/exit 0))

(defn- evaluate-args
  [args]
  (let  [{:keys [options summary]} (parse-opts args cli-options)]
    (cond
      (:help options) (exit (usage summary))
      :else options)))

(defn -main
  "Download Bing's image wallpaper."
  [& args]
  (let [{:keys [output-dir nb-images]} (evaluate-args args)]
    (run! println (map #(download-image output-dir %)
               (get-images nb-images)))))
