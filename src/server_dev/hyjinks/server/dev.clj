(ns hyjinks.server.dev
  (:require [ring.util.response :refer [response]]
            [compojure.core :refer [defroutes GET]]
            [hyjinks.core :refer [tag->string html head title import-js body div css]]))

(def page-frame
  (tag->string
    (html
      (head
        (title "Hyjinks Test Page")
        (import-js "js/compiled/hyjinks_browser.js"))
      (body
        (div {:id "results"} (css :font-size "30px" :font-weight "bold"))
        (div "Look at console for detailed results.")))))

(defroutes app-routes
  #_ (GET "/unit-tests" [] (response page-frame))
  (GET "/dom-demo"   [] (response "dom demo"))
  (GET "/react-demo" [] (response "react demo")))

(def app #'app-routes)
