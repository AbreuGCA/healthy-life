(ns healthy-life.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [healthy-life.routes.usda :refer [usda-routes]]
            [healthy-life.routes.ninja :refer [ninja-routes]]))

(defroutes app-routes
           usda-routes
           ninja-routes
           (GET "/" [] {:status 200 :body "API está online"})
           (route/not-found {:status 404 :body "Rota não encontrada"}))

(def app
  (-> app-routes
      (wrap-json-body {:keywords? true})
      wrap-json-response
      (wrap-defaults api-defaults)))
