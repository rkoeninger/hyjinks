(defproject hyjinks "0.0.3"
  :description "Hyjinks - An HTML generation/templating library"
  :url "http://github.com/rkoeninger/hyjinks"
  :license {
    :name "MIT License"
    :url "http://www.opensource.org/licenses/mit-license.php"
    :distribution :repo}
  :repositories [
    ["clojars" {:sign-releases false}]]
  :repl-options {:init-ns hyjinks.core}
  :source-paths ["src/shared" "src/cljs"]
  :clean-targets ^{:protect false} ["target" "logs" "resources/public/js/compiled"]
  :test-paths ["test/clj"]
  :jar-exclusions [#"public"]
  :cljsbuild {
    :builds {
      :dev {
        :source-paths ["src/shared" "src/cljs" "test/cljs"]
        :compiler {
          :output-to "resources/public/js/compiled/hyjinks_browser.js"
          :output-dir "resources/public/js/compiled/out"
          :asset-path "js/compiled/out"
          :optimizations :none
          :main hyjinks.browser-test
          :source-map true
          :source-map-timestamp true
          :cache-analysis true}}}}
  :plugins [
    [lein-cljsbuild "1.1.1"]
    [lein-figwheel "0.5.8"]]
  :dependencies [
    [org.clojure/clojure "1.8.0"]
    [org.clojure/clojurescript "1.8.51"]]
  :exclusions
    [org.clojure/clojure]
  :jvm-opts ["--add-modules" "java.xml.bind"]
  :figwheel {
    :server-port 3450
    :css-dirs ["resources/public/css"]
    :server-logfile "logs/figwheel_server.log"}
  :profiles {
    :dev {
      :dependencies [
        [ring/ring-core "1.4.0"]
        [ring-middleware-format "0.6.0"]
        [compojure "1.4.0"]
        [figwheel "0.4.0"]
        [org.omcljs/om "0.9.0" :exclusions [cljsjs/react]]
        [cljsjs/react-with-addons "0.13.3-0"]]}})
