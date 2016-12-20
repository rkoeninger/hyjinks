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
  :source-paths ["src/shared" "src/server_dev"]
  :clean-targets ^{:protect false} [
    "target"
    "logs"
    "resources/public/js/compiled"]
  :test-paths ["test"]
  :jar-exclusions [#"dev" #".html" #"public" #"server_dev"]
  :cljsbuild {
    :builds {
      :dev {
        :source-paths ["src/shared" "src/cljs" "src/cljs_dev"]
        :compiler {
          :output-to "resources/public/js/compiled/hyjinks_browser.js"
          :output-dir "resources/public/js/compiled/out"
          :asset-path "js/compiled/out"
          :optimizations :none
          :main hyjinks.browser.dev
          :source-map true
          :source-map-timestamp true
          :cache-analysis true}}
      :min {
        :source-paths ["src/shared" "src/cljs"]
        :compiler {
          :output-to "resources/public/js/compiled/hyjinks_browser.js"
          :optimizations :advanced
          :pretty-print false}}}}
  :plugins [
    [lein-cljsbuild "1.1.0"]
    [lein-figwheel "0.4.0"]]
  :dependencies [
    [org.clojure/clojure "1.7.0"]
    [org.clojure/clojurescript "1.7.122"]
    [ring/ring-core "1.4.0"]
    [ring-middleware-format "0.6.0"]
    [compojure "1.4.0"]
    [figwheel "0.4.0"]
    [org.omcljs/om "0.9.0" :exclusions [cljsjs/react]]
    [cljs-react-test "0.1.3-SNAPSHOT"]
    [cljsjs/react-with-addons "0.13.3-0"]]
  :exclusions
    [org.clojure/clojure]
  :figwheel {
    :http-server-root "public" ; default and assumes "resources"
    :server-port 3449
    ; :nrepl-port 7888
    :css-dirs ["resources/public/css"]
    :ring-handler hyjinks.server.dev/app
    :server-logfile "logs/figwheel_server.log"})
