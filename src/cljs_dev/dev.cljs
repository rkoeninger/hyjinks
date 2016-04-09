(ns hyjinks.react.dev
  (:require [hyjinks.core :as h :include-macros true]
            [hyjinks.react :as hr]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(enable-console-print!)

(defn root-view [data owner]
  (reify
    om/IRender
    (render [_]
      (hr/render-dom
        (h/div {:className "whatever"}
          (h/h1 (h/color "red") "Hello!"))))))

(defonce app-state (atom {}))

(set!
  (.-onload js/window)
  (fn []
    (om/root
      root-view
      app-state
      {:target (. js/document (getElementById "content"))})))
