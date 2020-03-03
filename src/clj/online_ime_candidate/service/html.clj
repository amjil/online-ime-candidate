(ns online-ime-candidate.service.html
  (:require
    [net.cgrand.enlive-html :as html]
    [clojure.string :as str]
    [clojure.walk :as walk]))

(defn burgud-content [url]
  (let [h-res (html/html-resource (java.net.URL. url))
        title (->> (html/select h-res [:div#MainContent :div.Article_Title])
                   html/texts
                   first)
        body (->> (html/select h-res [:div#MainContent :p])
                  html/texts)]
    [title body]))

(defn split-sentences [text]
  (->>
    (-> text
      (str/split #"᠂|᠃|\?|᠄"))
    (filter not-empty)
    (map str/trim)))

(defn replace-text [text]
  (-> text
    (str/replace #"  |  " " ")
    (str/replace #"" "")))

; (defn split-text [text]
  ; (str/split))
