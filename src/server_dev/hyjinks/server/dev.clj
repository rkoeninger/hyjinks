(ns hyjinks.server.dev
  (:require [ring.util.response :refer [file-response]]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :refer [resources]]))

(defroutes app-routes
  (GET "/" [] (file-response "resources/public/index.html"))
  (resources "/"))

(def app #'app-routes)
