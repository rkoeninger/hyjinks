(ns hyjinks.dom
  (:require [hyjinks.core :as h :include-macros true]))

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
              (doto e (.setAttribute (h/attr-name k) (str v))))
            (js/document.createElement (upper-case tag-name))
            attrs+css)
          items))
    f (js/document.createTextNode (str (f arg)))
    :else (js/document.createTextNode (str arg))))
