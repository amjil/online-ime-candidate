(ns online-ime-candidate.service.suffix
  (:require
    [clojure.string :as str]
    [clojure.java.jdbc :as jdbc]))

(def db {:classname   "org.sqlite.JDBC"
         :subprotocol "sqlite"
         :subname     "./database.db"})

(defn suffix [table suffix]
  (let [data (jdbc/query db [(str "select id, char_word from " table " where bqr_biclg like '%" suffix "'")])]
    (->> data
         (map :char_word)
         (str/join "\n")
         (spit (str table ">" suffix ".txt")))))

(defn prefix [table x]
  (let [data (jdbc/query db [(str "select id, char_word from " table " where bqr_biclg like '" x "%'")])]
    (->> data
         (map :char_word)
         (str/join "\n")
         (spit (str table "<" x ".txt")))))