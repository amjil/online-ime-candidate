(ns online-ime-candidate.middleware
  (:require
    [online-ime-candidate.env :refer [defaults]]
    [online-ime-candidate.config :refer [env]]
    [ring.middleware.params :refer [wrap-params]]
    [ring.middleware.json :refer [wrap-json-params wrap-json-body wrap-json-response]]
    [ring.middleware.keyword-params :refer [wrap-keyword-params]]
    [ring.middleware.cors :refer [wrap-cors]]))

; (defn wrap-base [handler]
;   (-> ((:middleware defaults) handler)
;       (wrap-defaults
;         (-> site-defaults
;             (assoc-in [:security :anti-forgery] false)
;             (assoc-in  [:session :store] (ttl-memory-store (* 60 30)))))))

(defn wrap-base [handler]
  (-> ((:middleware defaults) handler)
    (wrap-cors :access-control-allow-origin [#"http://localhost:9000" #"http://192.168.1.173:9000"]
               :access-control-allow-methods [:get :put :post :delete :POST]
               :access-control-allow-headers ["content-type"]
               :access-control-allow-credentials "true")
    ;; common wrapper
    wrap-keyword-params
    wrap-json-params
    wrap-params
    wrap-json-response))
