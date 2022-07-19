(ns bingo.core-test
  (:require
   [bingo.bing-api :as bing-api]
   [bingo.core :as sut]
   [clojure.java.io :as io]
   [clojure.string :as string]
   [clojure.test :refer [deftest is testing]]))

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
    (with-redefs [bing-api/fetch-images (fn [market nb-images] fetch-images-response)
                  bing-api/download-image (fn [_] (io/input-stream (java.io.File. "resources/sample.jpg")))]
      (is (= '("/tmp/ImageSample.jpeg")
             (sut/download-images {:mkt "fr-FR"
                                   :nb-images 1
                                   :output-dir "/tmp"}))))))

(deftest print-version
  (is (string/starts-with? (:exit-message (sut/evaluate-args ["--version"])) "Bingo version")))
