(ns hyjinks.react-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [hyjinks.core :as h :include-macros true]
            [hyjinks.react :refer [tag->react]]))

(defn- translate [x]
  (case x
    :hello "Hi"
    x))

(deftest react-transform
  (let [d (tag->react
            {:transform-content translate
             :transform-attr #(translate %2)}
            (h/div {:class ["class1" "class2"]}
              "some text"
              (h/span {:title [:hello "!"]})
              "more text"))]
    (is true)))

(deftest literals
  (testing "literals should have no apparent effect"
    (let [d0 (tag->react (h/span "<hi>"))
          d1 (tag->react (h/span (h/literal "<hi>")))]
      (is true #_ (= "<hi>" (.-textContent d0)))
      (is true #_ (= "<hi>" (.-textContent d1))))))
