(ns hyjinks.server.dev
  (:require [ring.util.response :refer [response]]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :refer [resources]]
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
  (GET "/" [] (response page-frame))
  (resources "/"))

(def app #'app-routes)
