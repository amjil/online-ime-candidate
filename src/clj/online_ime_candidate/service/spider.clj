(ns online-ime-candidate.service.spider
  (:use [etaoin.api])
  (:require
    [net.cgrand.enlive-html :as html]
    [clojure.string :as str]
    [clojure.walk :as walk]))


(def driver (chrome {:path-browser "/Applications/Google Chrome 2.app/Contents/MacOS/Google Chrome"}))
(go driver "http://mtg.mglip.com/")

(defn fn-prewalk
  [base]
  (let [fn-content #(if (map? %) (:content %) %)]
    (walk/prewalk fn-content base)))

(defn translate [x]
  (fill driver {:id "inputCyrillic_ID"} x)
  (click driver {:id "ButtonTran_ID"})
  (wait-visible driver {:id "outPutTraditonalM_ID"})
  (get-element-value driver {:id "outPutTraditonalM_ID"}))

(defn main [url]
  (let [h-res (html/html-resource (java.net.URL. url))
        title (-> (html/select h-res [:div.garqag_html])
                  first :content first)
        body (->> (html/select h-res [:div#agolga_yb :p])
                  (map #(fn-prewalk %) *1)
                  flatten
                  (remove nil?)
                  (str/join " "))]
    [title body]))
    ; [(translate title) (translate body)]))
