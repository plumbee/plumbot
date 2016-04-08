(ns com.plumbee.plumbot.system
  (:require [com.stuartsierra.component :as c]
            [com.plumbee.plumbot.register :refer [bot-config]]
            [com.plumbee.plumbot.components.debug :refer [map->Debug]]
            [com.plumbee.plumbot.components.websocket :refer [map->WebSocket]]
            [com.plumbee.plumbot.components.webapi :refer [map->WebApi]]
            [com.plumbee.plumbot.support.persistence :refer [load-data]]
            [com.plumbee.plumbot.components.timing :refer [->Timer]]
            [com.plumbee.plumbot.components.registry :refer [map->Registry]]))


(defn plumbot-system [slack-config]
  (c/system-map
    :bot-config bot-config
    :timer (->Timer)
    :webapi (map->WebApi slack-config)
    :websocket (c/using (map->WebSocket {}) [:timer :webapi])
    :registry (c/using (map->Registry {}) [:bot-config :timer :webapi :websocket])
    :debug (c/using (map->Debug {}) [:webapi])))
