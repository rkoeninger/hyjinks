(ns hyjinks.bootstrap
  (:require [hyjinks.core :refer [import-js import-css css div]]))

(defn import-bootstrap
  ([] (import-bootstrap "3.3.7"))
  ([version]
   [(import-js  (str "https://maxcdn.bootstrapcdn.com/bootstrap/" (name version) "/js/bootstrap.min.js"))
    (import-css (str "https://maxcdn.bootstrapcdn.com/bootstrap/" (name version) "/css/bootstrap.min.css"))]))

(def container (div {:className "container"}))

(def row (div {:className "row"}))

(defn grid-col [size span & content]
  (div {:className (str "col-" (name size) "-" span)} content))

(defn col-xs [size & content] (grid-col "xs" size content))
(defn col-sm [size & content] (grid-col "sm" size content))
(defn col-md [size & content] (grid-col "md" size content))
(defn col-lg [size & content] (grid-col "lg" size content))
(defn col-xl [size & content] (grid-col "xl" size content))
