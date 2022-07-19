(ns bingo.core
  (:require
   [bingo.bing-api :as bing]
   [clojure.java.io :as io]
   [clojure.string :as string]
   [clojure.tools.cli :as cli])
  (:import
   (java.util Locale))
  (:gen-class))

;;(set! *warn-on-reflection* true)       

(def mkt-codes (concat ["ar-SA" "da-DK" "de-AT" "de-CH" "de-DE" "en-AU" "en-CA" "en-GB"]
                       ["en-ID" "en-IN" "en-MY" "en-NZ" "en-PH" "en-US" "en-ZA" "es-AR"]
                       ["es-CL" "es-ES" "es-MX" "es-US" "fi-FI" "fr-BE" "fr-CA" "fr-CH"]
                       ["fr-FR" "it-IT" "ja-JP" "ko-KR" "nl-BE" "nl-NL" "pl-PL" "pt-PT"]
                       ["pt-br" "ru-RU" "sv-SE" "tr-TR" "zh-CN" "zh-HK" "zh-TW"]))

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
   ["-v" "--version" "Print bingo version number"]
   ["-h" "--help"]])

(defn- exit [status msg]
  (println msg)
  (System/exit status))

(defn create-image
  "Save Image File"
  [content filename]
  (try
    (io/make-parents filename)
    (io/copy content (java.io.File. filename))
    filename
    (catch Exception e
      (ex-info "Unable to save the file"
               {:filename filename}
               e))))

(defn url->filename [urlbase]
  (str (nth (re-find #"(.*OHR\.)(.*)(_.*)" urlbase) 2) ".jpeg"))

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

(defn- print-version []
  "Bingo version 1.0\n")

(defn evaluate-args
  [args]
  (let  [{:keys [options summary errors]} (cli/parse-opts args cli-options)]
    (cond
      (:help options) {:exit-message (usage summary) :ok? true}
      (:version options) {:exit-message (print-version) :ok? true}
      errors {:exit-message (error-msg errors)}
      :else options)))

(defn download-images
  "Downlaad Images"
  [params]
  (map #(create-image
         (bing/download-image (:url %))
         (str (:output-dir params) "/" (url->filename (:urlbase %))))
       (bing/fetch-images (:mkt params) (:nb-images params))))

(defn -main
  "Download Bing's image wallpaper."
  [& args]
  (let [{:keys [output-dir mkt nb-images exit-message ok?]} (evaluate-args args)]
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (run! println (download-images {:mkt mkt
                                      :nb-images nb-images
                                      :output-dir output-dir})))))

(comment
  (download-images {:mkt "fr-FR" :nb-images 7 :output-dir "/tmp"})
  (evaluate-args ["--version"]))
