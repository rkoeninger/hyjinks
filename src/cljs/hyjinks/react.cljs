(ns hyjinks.react
  (:require [clojure.string :refer [join]]
            [hyjinks.core :as h :include-macros true]))

(defn- attr-name [k]
  (case k
    :class :className
    k))

(defn- append-attr [e [k v]]
  (let [k (attr-name k)]
    (assoc e k
      (if (sequential? v)
        (join (flatten v))
        v))))

(defn tag->react [arg]
  (cond
    (h/tag? arg)
      (let [{:keys [tag-name attrs css items]} arg
            attrs+css (if (empty? css) attrs (assoc attrs :style css))]
        (.apply
          (aget js/React.DOM tag-name)
          nil
          (into-array
            (cons
              (clj->js (reduce append-attr {} attrs+css))
              (map tag->react items)))))
    (h/comment? arg)
      ""
    :else
      arg))
