(ns hyjinks.react-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [hyjinks.core :as h :include-macros true]
            [hyjinks.react :refer [tag->react]]))

(deftest react-transform
  (let [d (tag->react
            (h/div {:className ["class1" "class2"]}
              "some text"
              (h/span {:title [:hello "!"]})
              "more text")
            (fn [x] (case x :hello "Hi" x)))]
    (is true)))
