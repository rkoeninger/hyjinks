(ns hyjinks.react.dev
  (:require [hyjinks.core :as h :include-macros true]
            [hyjinks.react :as hr]
            [hyjinks.dom :as hd]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(enable-console-print!)

(def lang {
  :hello "Hello"
})

(defn translate [x]
  (if (keyword? x) (get lang x) x))

(defn root-view [data owner]
  (reify om/IRender
    (render [_]
      (let [t (h/tag "div.whatever"
                (h/h1 (h/color "red") :hello "!"))]
        (hr/tag->react t translate)))))

(defonce app-state (atom {}))

(set!
  (.-onload js/window)
  (fn []
    (om/root
      root-view
      app-state
      {:target (js/document.getElementById "react-content")})
    (.appendChild (js/document.getElementById "dom-content")
      (let [t (h/div {:className "whatever"}
                (h/h1 (h/color "blue") :hello "!"))]
        (hd/tag->dom t translate)))))
