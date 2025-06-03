(ns healthy-life.routes.storage
  (:require [compojure.core :refer :all]
            [healthy-life.state :refer [app-state]]))

;; ======== ROTAS DE ARMAZENAMENTO ========
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
               {:status 200 :body {:datas (sort datas)}})))