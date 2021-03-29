(ns bingo.core-test
  (:require [clojure.test :refer :all]
            [clojure.data.json :as json]
            [bingo.core :refer :all]))

(def image-metadata-response (json/read-str
  "{\"images\": [
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
}"))


(deftest get-images-test
  (testing "Get Images"
    (with-redefs [http-get (fn [url] image-metadata-response)]
      (testing "Only one images"
        (is (= 1 (count (get-images "fr-FR" 1)))))

      (testing "Image contains an URL and FileName"
        (is (= {:url "http://www.bing.com/th?id=OHR.MagneticIsland_FR-FR9412695841_1920x1080.jpg&rf=LaDigue_1920x1080.jpg&pid=hp"
                :filename "MagneticIsland.jpeg"}
             (first (get-images "fr-FR" 1))))))))
