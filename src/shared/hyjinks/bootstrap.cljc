(ns hyjinks.bootstrap
  (:require [hyjinks.core :refer [import-js import-css css div]]))

(defn- resource-url [version type]
  (str "https://maxcdn.bootstrapcdn.com/bootstrap/" (name version) "/" type "/bootstrap.min." type))

(defn import-bootstrap [version]
  [(import-js  (resource-url version "js"))
   (import-css (resource-url version "css"))])

(def container (div {:class "container"}))

(def row (div {:class "row"}))

(defn grid-col [screen-size width & content]
  (div {:class (str "col-" (name screen-size) "-" width)} content))

(defn col-xs [width & content] (grid-col "xs" width content))
(defn col-sm [width & content] (grid-col "sm" width content))
(defn col-md [width & content] (grid-col "md" width content))
(defn col-lg [width & content] (grid-col "lg" width content))
(defn col-xl [width & content] (grid-col "xl" width content))
