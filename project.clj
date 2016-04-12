(defproject hyjinks "0.0.2-SNAPSHOT"
  :description "Hyjinks - An HTML generation/templating library"
  :url "http://github.com/rkoeninger/hyjinks"
  :license {
    :name "MIT License"
    :url "http://www.opensource.org/licenses/mit-license.php"
    :distribution :repo}
  :repositories [
    ["clojars" {:sign-releases false}]]
  :repl-options {:init-ns hyjinks.core}
  :source-paths ["src/shared"]
  :clean-targets ^{:protect false} [
    "target"
    "logs"
    "resources/public/js/compiled"]
  :test-paths ["test"]
  :cljsbuild {
    :builds {
      :dev {
        :source-paths ["src/shared" "src/cljs" "src/cljs_dev"]
        :compiler {
          :output-to "resources/public/js/compiled/hyjinks_react.js"
          :output-dir "resources/public/js/compiled/out"
          :asset-path "js/compiled/out"
          :optimizations :none
          :main hyjinks.react.dev
          :source-map true
          :source-map-timestamp true
          :cache-analysis true}}
      :min {
        :source-paths ["src/shared" "src/cljs"]
        :compiler {
          :output-to "resources/public/js/compiled/hyjinks_react.js"
          :main hyjinks.react
          :optimizations :advanced
          :pretty-print false}}}}
  :plugins [
    [lein-cljsbuild "1.1.1"]]
  :dependencies [
    [org.clojure/clojure "1.8.0"]
    [org.clojure/clojurescript "1.8.40"]
    [org.omcljs/om "0.9.0"]]
  :exclusions
    [org.clojure/clojure])