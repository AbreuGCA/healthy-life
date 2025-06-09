(ns healthy-life.routes.ninja
  (:require [compojure.core :refer :all]
            [clj-http.client :as client]
            [healthy-life.routes.translate :refer [traduzir-texto]]))  ; Importa serviço de tradução

(def api-key "Z0BnVuK4EXYbLw3WcyKoOw==sMa1Uk7DsonyFJQF")
(def base-url "https://api.api-ninjas.com/v1/caloriesburned")

(defn buscar-atividades [atividade peso-kg duracao]
  (let [atividade-en (traduzir-texto atividade "pt" "en")
        peso-lb (* peso-kg 2.20462)
        params {:query-params {:activity atividade-en
                               :weight peso-lb
                               :duration duracao}
                :headers {"X-Api-Key" api-key}
                :throw-exceptions false
                :as :json}
        response (client/get base-url params)]
    (if (= 200 (:status response))
      (mapv #(update % :name traduzir-texto "en" "pt") (:body response))
      [])))

(defroutes ninja-routes
           (GET "/atividade" [atividade peso duracao]
             (let [peso (Double/parseDouble peso)
                   duracao (Double/parseDouble duracao)
                   variantes (buscar-atividades atividade peso duracao)
                   ;; Mantém apenas campos necessários (já traduzidos)
                   simplificado (mapv #(select-keys % [:name :duration_minutes :total_calories]) variantes)]
               {:status 200 :body {:variantes simplificado}})))