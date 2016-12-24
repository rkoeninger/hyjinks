(ns hyjinks.browser-test
  (:require [cljs.test :refer-macros [deftest is testing run-tests]]
            [hyjinks.core :as h :include-macros true]
            [hyjinks.dom :as hd]))

(enable-console-print!)

(deftest dom-transform
  (let [d (hd/tag->dom
            (h/div {:className ["class1" "class2"]}
              "some text"
              (h/span {:title [:hello "!"]})
              "more text")
            (fn [x] (case x :hello "Hi" x)))]
    (is (= "class1 class2" (.getAttribute d "class")))
    (is (= 3 (.-length (.-childNodes d))))
    (is (= "some text" (.-textContent (aget (.-childNodes d) 0))))
    (is (= "more text" (.-textContent (aget (.-childNodes d) 2))))
    (is (= "Hi!" (.getAttribute (aget (.-children d) 0) "title")))))

(defmethod cljs.test/report [:cljs.test/default :end-run-tests] [m]
  (let [success (cljs.test/successful? m)]
    (if js/window.callPhantom
      (js/window.callPhantom #js {:exitCode (if success 0 1)})
      (js/setTimeout
        #(let [results (js/document.getElementById "results")]
          (set! (.-innerHTML results) (if success "Success" "Failure")))
        1))))

(set! (.-onload js/window)
  #(run-tests 'hyjinks.browser-test))
