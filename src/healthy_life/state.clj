(ns healthy-life.state)

(defonce app-state (atom {:usuarios {}
                          :alimentos []
                          :exercicios []}))
