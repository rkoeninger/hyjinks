(ns hyjinks.dom
  (:use [clojure.string :only (join)])
  (:require [hyjinks.core :as h :include-macros true]))

(defn- apply-flat-join [f x]
  (cond
    (not f)         x
    (sequential? x) (join "" (map (comp str f) (flatten x)))
    :else           (f x)))

(defn tag->dom [arg & [f]]
  (cond
    (h/tag? arg)
      (let [{:keys [tag-name attrs css items]} arg
            attrs+css (if (empty? css) attrs (assoc attrs :style css))]
        (reduce
          (fn [e child]
            (doto e (.appendChild (tag->dom child f))))
          (reduce
            (fn [e [k v]]
              (doto e (.setAttribute (h/attr-name k) (apply-flat-join f v))))
            (js/document.createElement tag-name)
            attrs+css)
          items))
    f (js/document.createTextNode (str (f arg)))
    :else (js/document.createTextNode (str arg))))
