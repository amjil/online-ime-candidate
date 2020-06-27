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

(defn select-table [table-name]
  (let [sql-str (str "select id, char_word as word, bqr_biclg as total, giglgc as simple, active_order, '" table-name "' as tb  from " table-name " where 1=1 and category = 'normal'")
        data (jdbc/query db [sql-str])]
    data))

(def alpha
  ["a"        "e"        "i"        "m"        "t"        "x"
   "b"        "f"        "j"        "n"        "q"        "u"        "y"
   "c"        "g"       "k"        "o"        "r"        "v"        "z"
   "d"        "h"        "l"        "p"        "s"        "w"])

(defn query-next []
  (let [sql-str "select t1 || id1 as id1, t2 || id2 as id2 from phrase1 where 1=1"
        data (->> (jdbc/query db [sql-str])
                  (group-by :id1)
                  (map (fn [[k v]] (hash-map k (map :id2 v))))
                  (into {}))]
    data))

(defn get-candidate [db indexed id]
  (let [cand (get db id)]
    (if cand
      (map #(get indexed %) cand)
      [])))

(defn select-all []
  (let [data (for [x alpha] (select-table x))
        candidate (query-next)
        data
        (->> data
             flatten
             (keep-indexed #(assoc %2 :seq %1)))
        index-data (->> data
                        (map #(hash-map (str (:tb %) (:id %)) (:seq %)))
                        (into {}))]
    (map #(assoc % :child (get-candidate candidate index-data (str (:tb %) (:id %)))) data)))

    ; (group-by #(str (:tb %) (:id %)))))
