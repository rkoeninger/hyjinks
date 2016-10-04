(ns hyjinks.react
  (:use [clojure.string :only (join trim)])
  (:require [hyjinks.core :as h :include-macros true]))

(defn- apply-flat-join [f x]
  (cond
    (not f)         x
    (sequential? x) (join "" (map (comp trim str f) (flatten x)))
    :else           (f x)))

(defn tag->react [arg & [f]]
  (cond
    (h/tag? arg)
      (let [{:keys [tag-name attrs css items]} arg
            attrs+css (if (empty? css) attrs (assoc attrs :style css))
            attrs+css (reduce (fn [r [k v]] (assoc r k (apply-flat-join f v))) {} attrs+css)]
        (.apply (aget js/React.DOM tag-name) nil (into-array (cons (clj->js attrs+css) (map #(tag->react % f) items)))))
    f (f arg)
    :else arg))
