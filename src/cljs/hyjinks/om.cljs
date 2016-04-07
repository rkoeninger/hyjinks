(ns hyjinks.om
  (:require [hyjinks.core :as hy :include-macros true]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

; HyjinksTag -> OmDom
(defn render-dom [tag]
;	(apply dom/{:id tag} #js (:attrs-with-css tag)
  (:children tag)
)
