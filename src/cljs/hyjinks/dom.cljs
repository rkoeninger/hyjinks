(ns hyjinks.dom
  (:require [clojure.string :refer [join]]
            [hyjinks.core :as h :include-macros true]))

(defn- append-node [f e child]
  (doto e (.appendChild (f child))))

(defn- append-attr [f e [k v]]
  (doto e (.setAttribute
    (name k)
    (if (sequential? v)
      (let [v (flatten v)]
        (join (if f (map #(f k %) v) v)))
      (if f (f k v) v)))))

(defn tag->dom
  ([arg]
    (tag->dom {} arg))
  ([{:keys [transform-content transform-attr] :as opts} arg]
    (if (h/tag? arg)
      (let [{:keys [tag-name attrs css items]} arg
            attrs+css (if (empty? css) attrs (assoc attrs :style css))]
        (reduce
          (partial append-node (partial tag->dom opts))
          (reduce
            (partial append-attr transform-attr)
            (js/document.createElement tag-name)
            attrs+css)
          items))
      (js/document.createTextNode (str (if transform-content (transform-content arg) arg))))))
