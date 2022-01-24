(ns bingo.core
  (:require
   [bingo.bing-api :as bing]
   [bingo.http :as http]
   [clojure.java.io :as io]
   [clojure.string :as string]
   [clojure.tools.cli :as cli])
  (:import
   (java.util Locale))
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
   [nil "--mkt MARKET CODE" "Market code ex : fr-FR, en-US or de-DE"
    :default mkt-default
    :validate [#(validate-mkt %) #(str % " is not a valid market code")]]
   ["-h" "--help"]])

(defn- exit [status msg]
  (println msg)
  (System/exit status))

(defn save-file
  "Save Image File"
  [content filename output-dir]
  (let [output-file (str output-dir "/" filename)]
    (io/make-parents output-file)
    (io/copy content (java.io.File. output-file))
    output-file))

(defn create-img
  "Save Image File"
  [content filename]
  (try
    (io/make-parents filename)
    (io/copy content (java.io.File. filename))
    filename
    (catch Exception e)))

(defn download-image
  "Download image from url in output-dir"
  [out-dir {:keys [url filename]}]
  (-> (http/request-to url)
      (assoc :options {:as :stream})
      (http/http-get)
      :body
      (save-file filename out-dir)))

(defn url->filename [urlbase]
  (str (nth (re-find #"(.*OHR\.)(.*)(_.*)" urlbase) 2) ".jpeg"))

(defn create-image
  "Create Image"
  [url urlbase]
  (assoc {} :url (str bing-host url)
         :filename (url->filename urlbase)))

(defn json->images
  "Parse JSON"
  [json]
  (map #(create-image (get-in % ["url"]) (get-in % ["urlbase"]))
       (get-in  json ["images"])))

(defn get-images
  "Returns availables images"
  [mkt nb-images]
  (-> (http/request-to bing-host "/HPImageArchive.aspx?&format=js&idx=0&mkt=" mkt "&n=" nb-images)
      (http/http-get)
      (http/response)
      (json->images)))

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
      (:help options) {:exit-message (usage summary) :ok? true}
      errors {:exit-message (error-msg errors)}
      :else options)))

(defn download-images [market n output-dir]
  "Downlaad Images"
  (map #(create-img
         (bing/download-image (:url %))
         (str output-dir "/" (url->filename (:urlbase %))))
       (bing/fetch-images market n)))

(defn -main
  "Download Bing's image wallpaper."
  [& args]
  (let [{:keys [output-dir mkt nb-images exit-message ok?]} (evaluate-args args)]
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (run! println (download-images mkt nb-images output-dir)))))


