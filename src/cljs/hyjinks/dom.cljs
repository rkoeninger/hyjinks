(ns hyjinks.dom
  (:require [clojure.string :refer [join]]
            [hyjinks.core :as h :include-macros true]))

(defn- append-node [f e child]
  (doto e (.appendChild (f child))))

(defn- append-attr [e [k v]]
  (doto e
    (.setAttribute
      (name k)
      (if (sequential? v)
        (join (flatten v))
        v))))

(defn tag->dom [arg]
  (cond
    (h/tag? arg)
      (let [{:keys [tag-name attrs css items]} arg
            attrs+css (if (empty? css) attrs (assoc attrs :style css))]
        (reduce
          (partial append-node tag->dom)
          (reduce
            append-attr
            (js/document.createElement tag-name)
            attrs+css)
          items))
    (h/comment? arg)
      (js/document.createComment (:content arg))
    :else
      (js/document.createTextNode (str arg))))
