(ns bingo.core
  (:require [org.httpkit.client :as http]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.tools.cli :as cli]
            [clojure.string :as string])
  (:import (java.util Locale))
  (:gen-class))


(def mkt-codes (concat ["ar-SA" "da-DK" "de-AT" "de-CH" "de-DE" "en-AU" "en-CA" "en-GB"]
        ["en-ID" "en-IN" "en-MY" "en-NZ" "en-PH" "en-US" "en-ZA" "es-AR"]
        ["es-CL" "es-ES" "es-MX" "es-US" "fi-FI" "fr-BE" "fr-CA" "fr-CH"]
        ["fr-FR" "it-IT" "ja-JP" "ko-KR" "nl-BE" "nl-NL" "pl-PL" "pt-PT"]
        ["pt-br" "ru-RU" "sv-SE" "tr-TR" "zh-CN" "zh-HK" "zh-TW"]))

(def bing-host "http://www.bing.com")

(def mkt-default (-> (Locale/getDefault)
                     (.toString)
                     (.replace "_" "-")))

(defn- validate-mkt
  "validate MKT code"
  [mkt]
  (some #(= % mkt) mkt-codes))

(def cli-options
  [["-o" "--output-dir DIRECTORY" "Output Directory"
    :default "."]
   ["-n" "--nb-images NB IMAGES" "Numbers of images (max 7)"
    :default 1]
   ["-m" "--mkt MARKET CODE" "Market code ex : fr-FR, en-US or de-DE"
    :default mkt-default
    :validate [#(validate-mkt %) #(str % " is not a valid market code")]]
   ["-h" "--help"]])


(defn- exit [msg]
  (println msg)
  (System/exit 0))

(defn- exit-on-error [msg]
  (println msg)
  (System/exit 1))


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
  [url urlbase]
  (assoc {} :url (str bing-host url)
            :filename (str (nth (re-find #"(.*OHR\.)(.*)(_.*)" urlbase) 2) ".jpeg")))

(defn parse-json
  "Parse JSON"
  [json]
  (map #(create-image (get-in % ["url"]) (get-in % ["urlbase"]))
       (get-in  json ["images"])))

(defn http-get
  "Get image metadata as JSON"
  [url]
  (let [{:keys [status headers body error] :as resp} @(http/get url)]
    (cond error (println "Request failed " error)
          (not (= status 200)) (exit-on-error (format "Request %s failed with status %s" url status))
          :else (json/read-str body))))

(defn- get-images-url
  [nb-images mkt]
  (format "http://www.bing.com/HPImageArchive.aspx?&format=js&idx=0&mkt=%s&n=%s"
          mkt
          nb-images))

(defn get-images
  "Returns availables images"
  [mkt nb-images]
  (some->> mkt
     (get-images-url nb-images)
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
        "  bingo                  Download bing walpaper of the day in current directory"
        "  bingo -n 7 -o /tmp     Downlaod last 7 bing walpapers in /tmp directory"
        "  bingo -m fr-FR         Downlaod bing walpaper for fr-FR market in current directory"
        ""
        " For more informations on market code : "
        " https://docs.microsoft.com/rest/api/cognitiveservices-bingsearch/bing-images-api-v7-reference#market-codes"
        ""]
       (string/join \newline)))


(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn- evaluate-args
  [args]
  (let  [{:keys [options summary errors]} (cli/parse-opts args cli-options)]
    (cond
      (:help options) (exit (usage summary))
      errors (exit-on-error (error-msg errors))
      :else options)))

(defn -main
  "Download Bing's image wallpaper."
  [& args]
  (let [{:keys [output-dir mkt nb-images]} (evaluate-args args)]
    (run! println (map #(download-image output-dir %)
                       (get-images mkt nb-images)))))
