(ns hyjinks.react
  (:require [clojure.string :refer [join]]
            [hyjinks.core :as h :include-macros true]))

(defn- apply-flat-join [f x]
  (cond
    (not f)         x
    (sequential? x) (join "" (map (comp str f) (flatten x)))
    :else           (f x)))

(defn- attr-name [k]
  (case k
    :class :className
    k))

(defn tag->react [arg & [f]]
  (cond
    (h/tag? arg)
      (let [{:keys [tag-name attrs css items]} arg
            attrs+css (if (empty? css) attrs (assoc attrs :style css))
            attrs+css (reduce (fn [r [k v]] (assoc r (attr-name k) (apply-flat-join f v))) {} attrs+css)]
        (.apply (aget js/React.DOM tag-name) nil (into-array (cons (clj->js attrs+css) (map #(tag->react % f) items)))))
    (h/comment? arg) ""
    f (f arg)
    :else arg))
