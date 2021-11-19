(ns bingo.http
  (:require
   [clojure.data.json :as json]
   [org.httpkit.client :as http]))

(defn http-get
  "Performs HTTP GET"
  [request]
  @#_{:clj-kondo/ignore [:unresolved-var]}
   (http/get (:url request) (:opts request)))

(defn request-to
  "Build request"
  [host & path-items]
  {:url (apply str host path-items)})

(defn response
  "Create JSON response"
  [resp]
  (let [{:keys [status body error]} resp]
    (cond error (println "Request failed " error)
          (not (= status 200)) (println "Request failed with status :" status)
          :else (json/read-str body))))


