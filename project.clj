(defproject bingo "0.1.0-SNAPSHOT"
  :description "Bingo"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [http-kit/http-kit "2.5.3"]
                 [org.clojure/data.json "1.1.0"]
                 [org.clojure/tools.cli "1.0.206"]]


  :main bingo.core
  ;; :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev     {:plugins [[lein-shell "0.5.0"]]}}
  :aliases
  {"native"
   ["shell"
    "native-image"
    "--report-unsupported-elements-at-runtime"
    "--no-server"
    "--allow-incomplete-classpath"
    "--initialize-at-build-time"
    "--initialize-at-run-time=org.httpkit.client.ClientSslEngineFactory\\$SSLHolder"
    "--enable-url-protocols=http,https"
    "-jar" "./target/${:name}-${:version}-standalone.jar"
    "-H:Name=./target/${:name}"]

   "run-native" ["shell" "./target/${:name}"]}

  )
