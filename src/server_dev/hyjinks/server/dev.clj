(ns hyjinks.server.dev
  (:require [ring.util.response :refer [response]]
            [compojure.core :refer [defroutes GET]]
            [hyjinks.core :refer [tag->string html head title import-js body div css]]))

(defroutes app-routes
  (GET "/dom-demo"   [] (response "dom demo"))
  (GET "/react-demo" [] (response "react demo")))

(def app #'app-routes)
