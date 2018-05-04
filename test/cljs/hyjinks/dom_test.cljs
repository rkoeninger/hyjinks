(ns hyjinks.dom-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [hyjinks.core :as h :include-macros true]
            [hyjinks.dom :refer [tag->dom]]))

(deftest unflattened-values
  (let [d (tag->dom
            (h/div {:class ["class1" "class2"]}
              "some text"
              (h/span {:title "Hi!"})
              "more text"))]
    (is (= "class1 class2" (.getAttribute d "class")))
    (is (= 3 (.-length (.-childNodes d))))
    (is (= "some text" (.-textContent (aget (.-childNodes d) 0))))
    (is (= "more text" (.-textContent (aget (.-childNodes d) 2))))
    (is (= "Hi!" (.getAttribute (aget (.-children d) 0) "title")))))

(deftest literals
  (testing "literals should have no apparent effect"
    (let [d0 (tag->dom (h/span "<hi>"))
          d1 (tag->dom (h/span (h/literal "<hi>")))]
      (is (= "<hi>" (.-textContent d0)))
      (is (= "<hi>" (.-textContent d1))))))
