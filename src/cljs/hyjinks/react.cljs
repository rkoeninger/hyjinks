(ns hyjinks.react
  (:require [hyjinks.core :as h :include-macros true]))

(defn tag->react [arg & [f]]
  (cond
    (h/tag? arg)
      (let [{:keys [tag-name attrs css items]} arg
            attrs+css (if (empty? css) attrs (assoc attrs :style css))]
        (.apply (aget js/React.DOM tag-name) nil (into-array (cons (clj->js attrs+css) (map #(tag->react % f) items)))))
    f (f arg)
    :else arg))
