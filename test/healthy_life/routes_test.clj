(ns healthy-life.routes-test
  (:require [cheshire.core :as json]
            [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [healthy-life.handler :refer [app]]))

;; ========== Testes USDA ==========
(deftest test-usda-busca-alimentos
  (testing "GET /buscar-alimentos"
    (let [response (app (mock/request :get "/buscar-alimentos?termo=banana"))
          body (:body response)]
      (is (= 200 (:status response)))
      (is (string? body)))))

(deftest test-usda-calorias-100g
  (testing "GET /calorias-100g/:fdcId"
    ;; usa um FDC ID conhecido (exemplo: 1102657 para "Banana, raw")
    (let [response (app (mock/request :get "/calorias-100g/1102657"))]
      (is (= 200 (:status response))))))

;; ========== Testes NINJA ==========
(deftest test-ninja-atividade
  (testing "GET /atividade"
    (let [response (app (mock/request :get "/atividade?atividade=running&peso=70&duracao=30"))]
      (is (= 200 (:status response))))))

(deftest test-ninja-calorias-post
  (testing "POST /calorias-atividade"
    (let [request (-> (mock/request :post "/calorias-atividade")
                      (mock/json-body {:name "Running"
                                       :duration_minutes 30
                                       :total_calories 420}))
          response (app request)
          parsed-body (json/parse-string (:body response) true)]
      (is (= 200 (:status response)))
      (is (= {:atividade "Running"
              :duracao 30
              :calorias 420}
             parsed-body)))))
