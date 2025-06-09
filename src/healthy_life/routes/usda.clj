(ns healthy-life.routes.usda
  (:require [compojure.core :refer :all]
            [clj-http.client :as client]
            [cheshire.core :as json]
            [healthy-life.routes.translate :refer [traduzir-texto]]))  ; Importa serviço de tradução

(def api-key "9TQiNewIqPmRVRCFOy5vdR8Ld3ktO75Bxy6OWLmj")
(def base-url "https://api.nal.usda.gov/fdc/v1")

;; ======== FUNÇÃO AUSENTE ADICIONADA ========
(defn calcular-calorias [kcal100g gramas]
  (* (/ kcal100g 100.0) gramas))

(defn buscar-alimentos [termo]
  (let [termo-en (traduzir-texto termo "pt" "en")  ; Traduz termo PT→EN
        params   {:query     termo-en
                  :pageSize  10
                  :dataType ["SR Legacy"]
                  :api_key   api-key}
        response (client/get (str base-url "/foods/search") {:query-params params})
        dados    (json/parse-string (:body response) true)
        alimentos (get dados :foods)]
    ;; Traduz todas as descrições EN→PT
    (mapv #(update % :description traduzir-texto "en" "pt") alimentos)))

(defn calorias-por-100g [alimento]
  (let [nutrientes (:foodNutrients alimento)
        energia    (first (filter #(= "Energy" (get-in % [:nutrient :name])) nutrientes))]
    (if energia
      (let [value (or (:amount energia) 0)
            unit  (get-in energia [:nutrient :unitName] "")]
        (cond
          (re-matches #"(?i)kcal" unit) (double value)
          (re-matches #"(?i)kj" unit)   (double (/ value 4.184))
          :else -1))
      -2)))

(defroutes usda-routes
           (GET "/buscar-alimentos" [termo]
             (let [resultados (buscar-alimentos termo)
                   ;; Mantém apenas campos necessários (já traduzidos)
                   simplificados (mapv #(select-keys % [:description :fdcId]) resultados)]
               {:status 200 :body {:alimentos simplificados}}))

           (GET "/calorias-100g/:fdcId" [fdcId]
             (let [response (client/get (str base-url "/food/" fdcId)
                                        {:query-params {:api_key api-key}})
                   alimento (json/parse-string (:body response) true)
                   kcal100g (calorias-por-100g alimento)
                   ;; Traduz descrição do alimento EN→PT
                   descricao-pt (traduzir-texto (:description alimento) "en" "pt")]
               {:status 200 :body {:descricao     descricao-pt
                                   :kcal-por-100g kcal100g}})))