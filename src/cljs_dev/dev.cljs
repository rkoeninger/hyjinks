(ns hyjinks.browser.dev
  (:require [cljs.test :refer-macros [deftest is testing run-tests]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [hyjinks.core :as h :include-macros true]
            [hyjinks.react :as hr]
            [hyjinks.dom :as hd]))

(enable-console-print!)

(deftest dom-transform (is true)
  (let [d (hd/tag->dom
            (h/div {:className ["class1" "class2"]}
              "some text"
              (h/span {:title [:hello "!"]})
              "more text")
            (fn [x] (case x :hello "Hi" x)))]
    (is (= 3 (.-length (.-childNodes d))))
    (is (= "some text" (.-textContent (aget (.-childNodes d) 0))))
    (is (= "more text" (.-textContent (aget (.-childNodes d) 2))))
    (is (= "Hi!" (.getAttribute (aget (.-children d) 0) "title")))))

(defmethod cljs.test/report [:cljs.test/default :end-run-tests] [m]
  (let [success (cljs.test/successful? m)]
    (if js/window.callPhantom
      (js/window.callPhantom #js {:exit (if success 0 1)})
      (js/setTimeout
        #(set! (.-innerHTML (js/document.getElementById "results")) (if success "Success" "Failure"))
        1000))))

(set! (.-onload js/window)
  (run-tests 'hyjinks.browser.dev))
