(ns hyjinks.react
  (:require [hyjinks.core :as h :include-macros true]))

; HyjinksTag -> ReactDom
(defn render-dom [arg] ; TODO: needs (f: any -> any) to selectively transform elements
  (if (h/tag? arg)
    (let [{:keys [tag-name attrs css items]} arg
          attrs+css (if (empty? css) attrs (assoc attrs :style css))]
      (.apply (aget js/React.DOM tag-name) nil (into-array (cons (clj->js attrs+css) (map render-dom items)))))
    arg))
