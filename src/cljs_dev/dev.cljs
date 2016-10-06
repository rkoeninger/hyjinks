(ns hyjinks.browser.dev
  (:require [cljs.test :refer-macros [deftest is testing run-tests]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [hyjinks.core :as h :include-macros true]
            [hyjinks.react :as hr]
            [hyjinks.dom :as hd]))

(enable-console-print!)

(deftest sample-test
  (testing "should always pass"
    (is true))

  #_ (testing "should always fail"
    (is false)))

(defn end [success]
  (if js/window.callPhantom
    (js/window.callPhantom #js {:exit (if success 0 1)})
    (set! (.-innerHTML (js/document.getElementById "results")) (if success "Success" "Failure"))))

(defn run []
  (run-tests 'hyjinks.browser.dev))

(defmethod cljs.test/report [:cljs.test/default :end-run-tests] [m]
  (end (cljs.test/successful? m)))

(set! (.-onload js/window) run)
