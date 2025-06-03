(ns healthy-life.routes.storage
  (:require [compojure.core :refer :all]
            [clojure.string :as str]
            [healthy-life.state :refer [app-state]]))

(defn entre-datas? [data inicio fim]
  (and (not (str/blank? data))
       (>= (compare data inicio) 0)
       (<= (compare data fim) 0)))

(defroutes storage-routes
           (POST "/salvar-usuario" request
             (let [usuario (:body request)]
               (swap! app-state assoc-in [:usuarios (:nome usuario)] usuario)
               {:status 200 :body {:mensagem "Usuário salvo com sucesso!"}}))

           (POST "/adicionar-alimento" request
             (let [alimento (:body request)]
               (swap! app-state update :alimentos conj alimento)
               {:status 200 :body {:mensagem "Alimento registrado!"}}))

           (POST "/adicionar-exercicio" request
             (let [exercicio (:body request)]
               (swap! app-state update :exercicios conj exercicio)
               {:status 200 :body {:mensagem "Exercício registrado!"}}))

           (GET "/obter-dados" []
             {:status 200 :body @app-state})

           (GET "/datas-disponiveis" []
             (let [dados @app-state
                   datas-alimentos (distinct (map :data (:alimentos dados)))
                   datas-exercicios (distinct (map :data (:exercicios dados)))
                   datas (distinct (concat datas-alimentos datas-exercicios))]
               {:status 200 :body {:datas (sort datas)}}))

           (GET "/extrato" [inicio fim]
             (let [{:keys [usuarios alimentos exercicios]} @app-state
                   alimentos-filtrados (filter #(entre-datas? (:data %) inicio fim) alimentos)
                   exercicios-filtrados (filter #(entre-datas? (:data %) inicio fim) exercicios)]
               {:status 200
                :body {:usuarios usuarios
                       :alimentos alimentos-filtrados
                       :exercicios exercicios-filtrados}}))

           (GET "/saldo" [inicio fim]
             (let [{:keys [alimentos exercicios]} @app-state
                   alimentos-filtrados (filter #(entre-datas? (:data %) inicio fim) alimentos)
                   exercicios-filtrados (filter #(entre-datas? (:data %) inicio fim) exercicios)
                   total-alimentos (reduce + (map :kcal alimentos-filtrados))
                   total-exercicios (reduce + (map :calorias exercicios-filtrados))]
               {:status 200
                :body {:consumido total-alimentos
                       :gasto total-exercicios
                       :saldo (- total-alimentos total-exercicios)}})))
