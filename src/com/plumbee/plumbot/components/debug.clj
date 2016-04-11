(ns com.plumbee.plumbot.components.debug
  (:require [com.stuartsierra.component :as c]
            [com.plumbee.plumbot.support.logging :as log]
            [com.plumbee.plumbot.components.webapi :refer [users-setPresence]])
  (:import (java.net InetAddress)))


(def hostname (.. InetAddress getLocalHost getHostName))


(defn on-start [webapi]
  (log/always (str "Plumbot booted on " hostname "<br />"))
  (users-setPresence webapi "auto"))

(defn on-stop [webapi]
  (log/always (str "Plumbot shutdown on " hostname "<br />"))
  (users-setPresence webapi "away"))

(defrecord Debug [webapi]

  c/Lifecycle

  (start [unstarted]
    (log/always \^ (.getSimpleName (type unstarted)))
    (on-start webapi)
    unstarted)

  (stop [unstopped]
    (log/always \_ (.getSimpleName (type unstopped)))
    (on-stop webapi)
    unstopped))
