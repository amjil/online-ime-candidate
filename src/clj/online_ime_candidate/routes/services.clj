(ns online-ime-candidate.routes.services
  (:require
    [reitit.swagger :as swagger]
    [reitit.swagger-ui :as swagger-ui]
    [reitit.ring.coercion :as coercion]
    [reitit.coercion.spec :as spec-coercion]
    [reitit.ring.middleware.muuntaja :as muuntaja]
    [reitit.ring.middleware.multipart :as multipart]
    [reitit.ring.middleware.parameters :as parameters]
    [online-ime-candidate.middleware.formats :as formats]
    [online-ime-candidate.middleware.exception :as exception]
    [ring.util.http-response :refer :all]
    [clojure.java.io :as io]
    [clj-http.client :as client]
    [cheshire.core :as cheshire]
    [clojure.tools.logging :as log]
    [clojure.java.jdbc :as jdbc]
    [clojure.string :as str]))

(def db {:classname   "org.sqlite.JDBC"
         :subprotocol "sqlite"
         :subname     "./database.db"})


(defn candidate [candstr]
  (let [en-cands (str/split candstr #",")
        cands (map #(str/lower-case %) en-cands)
        starts (map #(str %) (into #{} (map #(str (first %)) cands)))
        condition-str (str/join "," (map #(str "'" % "'") cands))
        mn-cands (flatten (map #(jdbc/query db [(str "select id, char_word, bqr_biclg, giglgc, case when active_order is not null then active_order else 0 end as active_order, '" % "' as tb  from " % " where giglgc in (" condition-str ")")]) starts))]
        ; data (if (empty? mn-cands) [] (sort-by :active_order > mn-cands))]
    ; data))
    mn-cands))

(defn bqr-candidate [candstr]
  (let [table (first candstr)
        sql-str (str "select id, char_word, bqr_biclg, giglgc, active_order, '" table "' as tb  from " table " where bqr_biclg = ?")
        data (jdbc/query db [sql-str candstr])]
    data))

(defn bqr-update []
  (let [sql-str "update r set active_order = 1 where id = 241"
        result (jdbc/update! db "r" {:active_order 1} ["id = 1"])]
    result))

(defn service-routes []
  ["/api"
   {:coercion spec-coercion/coercion
    :muuntaja formats/instance
    :swagger {:id ::api}
    :middleware [;; query-params & form-params
                 parameters/parameters-middleware
                 ;; content-negotiation
                 muuntaja/format-negotiate-middleware
                 ;; encoding response body
                 muuntaja/format-response-middleware
                 ;; exception handling
                 exception/exception-middleware
                 ;; decoding request body
                 muuntaja/format-request-middleware
                 ;; coercing response bodys
                 coercion/coerce-response-middleware
                 ;; coercing request parameters
                 coercion/coerce-request-middleware
                 ;; multipart
                 multipart/multipart-middleware]}

   ;; swagger documentation
   ["" {:no-doc true
        :swagger {:info {:title "my-api"
                         :description "https://cljdoc.org/d/metosin/reitit"}}}

    ["/swagger.json"
     {:get (swagger/create-swagger-handler)}]

    ["/api-docs/*"
     {:get (swagger-ui/create-swagger-ui-handler
             {:url "/api/swagger.json"
              :config {:validator-url nil}})}]]

   ["/ping"
    {:get (constantly (ok {:message "pong"}))}]

   ["/candidate"
    {:get {
           :parameters {:query {:input string?}}
           :handler (fn [{{{:keys [input]} :query} :parameters}]
                      {:status 200
                       :body (candidate input)})}}]


   ["/math"
    {:swagger {:tags ["math"]}}

    ["/plus"
     {:get {:summary "plus with spec query parameters"
            :parameters {:query {:x int?, :y int?}}
            :responses {200 {:body {:total pos-int?}}}
            :handler (fn [{{{:keys [x y]} :query} :parameters}]
                       {:status 200
                        :body {:total (+ x y)}})}
      :post {:summary "plus with spec body parameters"
             :parameters {:body {:x int?, :y int?}}
             :responses {200 {:body {:total pos-int?}}}
             :handler (fn [{{{:keys [x y]} :body} :parameters}]
                        {:status 200
                         :body {:total (+ x y)}})}}]]

   ["/files"
    {:swagger {:tags ["files"]}}

    ["/upload"
     {:post {:summary "upload a file"
             :parameters {:multipart {:file multipart/temp-file-part}}
             :responses {200 {:body {:name string?, :size int?}}}
             :handler (fn [{{{:keys [file]} :multipart} :parameters}]
                        {:status 200
                         :body {:name (:filename file)
                                :size (:size file)}})}}]

    ["/download"
     {:get {:summary "downloads a file"
            :swagger {:produces ["image/png"]}
            :handler (fn [_]
                       {:status 200
                        :headers {"Content-Type" "image/png"}
                        :body (-> "public/img/warning_clojure.png"
                                  (io/resource)
                                  (io/input-stream))})}}]]])
