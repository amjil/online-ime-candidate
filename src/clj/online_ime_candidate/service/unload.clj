(ns online-ime-candidate.service.unload
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

(defn trim-word [word]
  (if (str/starts-with? word " ")
    (str " " (str/trim word))
    word))

(defn unload-to-file [table-name]
  (let [sql-str (str "select id, char_word as word, bqr_biclg as total, giglgc as simple, active_order, '" table-name "' as tb  from " table-name " where 1=1")
        data (jdbc/query db [sql-str])]
    (->> data
         (map #(str (trim-word (:word %)) " -- " (:total %)))
         (str/join "\n")
         (spit (str table-name ".txt")))))

(defn select-all [table-name]
  (let [sql-str (str "select id, char_word as word, bqr_biclg as total, giglgc as simple, active_order, '" table-name "' as tb  from " table-name " where 1=1 and category = 'normal'")
        data (jdbc/query db [sql-str])]
    data))
