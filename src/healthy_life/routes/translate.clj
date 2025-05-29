(ns healthy-life.routes.translate
  (:require [compojure.core :refer :all]
            [clj-http.client :as client]
            [clojure.string :as str]))

(defn traduzir-texto [texto de para]
  (let [url "https://api.mymemory.translated.net/get"
        response (client/get url {:query-params {"q" texto "langpair" (str de "|" para)}
                                  :as :json})
        traduzido (get-in response [:body :responseData :translatedText])]
    (if (str/blank? traduzido) texto traduzido)))

(defroutes translate-routes
           (GET "/translate" [texto de para]
             {:status 200
              :body {:traduzido (traduzir-texto texto de para)}}))