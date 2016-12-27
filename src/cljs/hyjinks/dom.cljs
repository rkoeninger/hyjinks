(ns hyjinks.dom
  (:require [clojure.string :refer [join]]
            [hyjinks.core :as h :include-macros true]))

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
              (doto e (.setAttribute (name k) (apply-flat-join f v))))
            (js/document.createElement tag-name)
            attrs+css)
          items))
    (h/comment? arg) (js/document.createComment (:content arg))
    f (js/document.createTextNode (str (f arg)))
    :else (js/document.createTextNode (str arg))))
