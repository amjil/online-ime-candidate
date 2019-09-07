(ns online-ime-candidate.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [online-ime-candidate.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[online-ime-candidate started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[online-ime-candidate has shut down successfully]=-"))
   :middleware wrap-dev})
