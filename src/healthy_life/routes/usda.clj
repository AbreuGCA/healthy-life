(ns healthy-life.routes.usda
  (:require [compojure.core :refer :all]
            [clj-http.client :as client]
            [cheshire.core :as json]))

(def api-key "9TQiNewIqPmRVRCFOy5vdR8Ld3ktO75Bxy6OWLmj")
(def base-url "https://api.nal.usda.gov/fdc/v1")

(defn buscar-alimentos [termo]
  (let [params   {:query     termo
                  :pageSize  10
                  :dataType ["SR Legacy"]
                  :api_key   api-key}
        response (client/get (str base-url "/foods/search") {:query-params params})
        dados    (json/parse-string (:body response) true)]
    (get dados :foods)))

(defn calorias-por-100g [alimento]
  (let [nutrientes (:foodNutrients alimento)
        energia    (first (filter #(= "Energy" (get-in % [:nutrient :name])) nutrientes))]
    (if energia
      (let [value (or (:amount energia) 0)
            unit (get-in energia [:nutrient :unitName] "")]
        ;DEBUG -> (println "Energia encontrada -" value unit)
        (cond
          (re-matches #"(?i)kcal" unit) (double value)
          (re-matches #"(?i)kj" unit) (double (/ value 4.184))
          :else (do (println "Unidade desconhecida:" unit) 0.0)))
      ;DEBUG -> (do (println "DEBUG: Nenhum 'Energy' encontrado") 0.0)
      )))


(defn calcular-calorias [kcal100g gramas]
  (* (/ kcal100g 100.0) gramas))

(defroutes usda-routes
           (GET "/buscar-alimentos" [termo]
             (let [resultados     (buscar-alimentos termo)
                   simplificados  (mapv #(select-keys % [:description :fdcId]) resultados)]
               {:status 200 :body {:alimentos simplificados}}))

           (GET "/calorias-100g/:fdcId" [fdcId]
             (let [response (client/get (str base-url "/food/" fdcId)
                                        {:query-params {:api_key api-key}})
                   alimento (json/parse-string (:body response) true)
                   kcal100g (calorias-por-100g alimento)]
               {:status 200 :body {:descricao   (:description alimento)
                                   :kcal-por-100g kcal100g}}))

           (POST "/calcular-calorias" request
             (let [{:keys [fdcId gramas]} (:body request)
                   response     (client/get (str base-url "/food/" fdcId)
                                            {:query-params {:api_key api-key}})
                   alimento     (json/parse-string (:body response) true)
                   kcal100g     (calorias-por-100g alimento)
                   calorias     (calcular-calorias kcal100g gramas)]
               {:status 200
                :body   {:descricao (:description alimento)
                         :gramas    gramas
                         :kcal      (Math/round calorias)}})))
