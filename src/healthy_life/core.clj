(ns healthy-life.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [healthy-life.handler :refer [app]]))

(defn -main []
  (println "🟢 API iniciando em http://localhost:3000")
  (run-jetty app {:port 3000}))