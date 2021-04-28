(ns bingo.http
  (:require [org.httpkit.client :as http]
            [clojure.data.json :as json]))

(defn http-get
  "Performs HTTP GET"
  [request]
  @(http/get (:url request) (:opts request)))

(defn request-to
  "Build request"
  [host & path-items]
  {:url (apply str host path-items)})

(defn response [resp]
  "Create JSON response"
  (let [{:keys [status headers body error]} resp]
    (cond error (println "Request failed " error)
          (not (= status 200)) (println "Request failed with status :" status)
          :else (json/read-str body))))
