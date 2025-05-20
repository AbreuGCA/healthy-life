(ns healthy-life.routes.ninja
  (:require [compojure.core :refer :all]
            [clj-http.client :as client]))

(def api-key "Z0BnVuK4EXYbLw3WcyKoOw==sMa1Uk7DsonyFJQF")
(def base-url "https://api.api-ninjas.com/v1/caloriesburned")

(defn buscar-atividades [atividade peso duracao]
  (let [params {:query-params {:activity atividade
                               :weight peso
                               :duration duracao}
                :headers {"X-Api-Key" api-key}
                :throw-exceptions false
                :as :json}
        response (client/get base-url params)]
    (case (:status response)
      200 (:body response)
      404 []
      [])))

(defroutes ninja-routes
           (GET "/atividade" [atividade peso duracao]
             (let [peso (Double/parseDouble peso)
                   duracao (Double/parseDouble duracao)
                   variantes (buscar-atividades atividade peso duracao)
                   simplificado (mapv #(select-keys % [:name :duration_minutes :total_calories]) variantes)]
               {:status 200 :body {:variantes simplificado}}))

           (POST "/calorias-atividade" request
             (let [{:keys [name duration_minutes total_calories]} (:body request)]
               {:status 200
                :body {:atividade name
                       :duracao duration_minutes
                       :calorias total_calories}})))
