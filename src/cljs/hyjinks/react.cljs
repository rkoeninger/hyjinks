(ns hyjinks.react
  (:require [clojure.string :refer [join]]
            [hyjinks.core :as h :include-macros true]))

(defn- attr-name [k]
  (case k
    :class :className
    k))

(defn- append-attr [f e [k v]]
  (let [k (attr-name k)]
    (assoc e k
      (if (sequential? v)
        (let [v (flatten v)]
          (join (if f (map #(f k %) v) v)))
        (if f (f k v) v)))))

(defn tag->react
  ([arg]
    (tag->react {} arg))
  ([{:keys [transform-content transform-attr] :as opts} arg]
    (cond
      (h/tag? arg)
        (let [{:keys [tag-name attrs css items]} arg
              attrs+css (if (empty? css) attrs (assoc attrs :style css))]
          (.apply
            (aget js/React.DOM tag-name)
            nil
            (into-array
              (cons
                (clj->js (reduce (partial append-attr transform-attr) {} attrs+css))
                (map (partial tag->react opts) items)))))
      (h/comment? arg)
        ""
      :else
        (if transform-content (transform-content arg) arg))))