(ns hyjinks.browser-test
  (:require [cljs.test :refer [report successful?] :refer-macros [run-tests]]
            [hyjinks.dom-test]
            [hyjinks.react-test]))

(enable-console-print!)

(defmethod report [:cljs.test/default :end-run-tests] [m]
  (let [success (successful? m)]
    (if js/window.callPhantom
      (js/window.callPhantom #js {:exitCode (if success 0 1)})
      (js/setTimeout
        #(let [results (js/document.getElementById "results")]
          (set! (.-innerHTML results) (if success "Success" "Failure")))
        1))))

(set! (.-onload js/window)
  #(run-tests
    'hyjinks.dom-test
    'hyjinks.react-test))
