(ns bingo.core-test
  (:require
   [bingo.core :refer :all]
   [bingo.http :as http]
   [clojure.test :refer :all]))

(def image-metadata-response
  {:status 200
   :body   "{\"images\": [
    {
      \"startdate\": \"20210319\",
      \"fullstartdate\": \"202103190700\",
      \"enddate\": \"20210320\",
      \"url\": \"/th?id=OHR.MagneticIsland_FR-FR9412695841_1920x1080.jpg&rf=LaDigue_1920x1080.jpg&pid=hp\",
      \"urlbase\": \"/th?id=OHR.MagneticIsland_FR-FR9412695841\",
      \"copyright\": \"Aerial view of the City of Adelaide shipwreck with trees growing on it, Magnetic Island, Queensland, Australia (© Amazing Aerial Agency/Offset by Shutterstock)\",
      \"copyrightlink\": \"https://www.bing.com/search?q=The+City+of+Adelaide+shipwreck+magnetic+island&form=hpcapt&filters=HpDate%3a%2220210319_0700%22\",
      \"title\": \"Life carries on, rising from a ship's skeleton\",
      \"quiz\": \"/search?q=Bing+homepage+quiz&filters=WQOskey:%22HPQuiz_20210319_MagneticIsland%22&FORM=HPQUIZ\",
      \"wp\": true,
      \"hsh\": \"dcc8e01a6dc6ddb333f323619e967345\",
      \"drk\": 1,
      \"top\": 1,
      \"bot\": 1,
      \"hs\": []
    }
  ],
  \"tooltips\": {
    \"loading\": \"Chargement en cours...\",
    \"previous\": \"Image précédente\",
    \"next\": \"Image suivante\",
    \"walle\": \"Cette image n’est pas disponible pour téléchargement en tant que papier peint.\",
    \"walls\": \"Téléchargez cette image. L’utilisation de cette image est réservée à des fins de papier peint uniquement.\"
  }
}"})

(deftest get-images-test
  (testing "Get Images"
    (with-redefs [http/http-get (fn [_] image-metadata-response)]
      (testing "Only one images"
        (is (= 1 (count (get-images "fr-FR" 1)))))

      (testing "Image contains an URL and FileName"
        (is (= {:url "http://www.bing.com/th?id=OHR.MagneticIsland_FR-FR9412695841_1920x1080.jpg&rf=LaDigue_1920x1080.jpg&pid=hp"
                :filename "MagneticIsland.jpeg"}
               (first (get-images "fr-FR" 1))))))))

(def fetch-images-response
  [{:hsh "7e19aa7e3acaf5f9a82fec48fc18448e",
    :urlbase "/th?id=OHR.ImageSample_FR-FR0080076119",
    :bot 1,
    :hs [],
    :top 1,
    :copyrightlink
    "https://www.bing.com/search?q=Manhattan+New+York&form=hpcapt&filters=HpDate%3a%2220220123_2300%22",
    :copyright
    "Bas de Manhattan à New York, États-Unis (© New York On Air/Offset/Shutterstock)",
    :title "New York, New York",
    :enddate "20220124",
    :fullstartdate "202201232300",
    :startdate "20220123",
    :url
    "/th?id=OHR.ImageSample_FR-FR0080076119_1920x1080.jpg&rf=LaDigue_1920x1080.jpg&pid=hp",
    :drk 1,
    :wp true,
    :quiz
    "/search?q=Bing+homepage+quiz&filters=WQOskey:%22HPQuiz_20220123_ImageSample%22&FORM=HPQUIZ"}])

(deftest download-bing-images
  (testing "Download 1 image"
    (with-redefs [bingo.bing-api/fetch-images (fn [market nb-images] fetch-images-response)
                  bingo.bing-api/download-image (fn [_] (clojure.java.io/input-stream (java.io.File. "resources/sample.jpg")))]
      (is (= '("/tmp/ImageSample.jpeg")
             (download-images "fr-FR" 1 "/tmp"))))))
