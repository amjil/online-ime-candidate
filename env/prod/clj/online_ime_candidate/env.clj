(ns online-ime-candidate.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[online-ime-candidate started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[online-ime-candidate has shut down successfully]=-"))
   :middleware identity})
