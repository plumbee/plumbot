;
; Copyright © 2016 Plumbee Ltd.
;
; Licensed under the Apache License, Version 2.0 (the "License");
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
; http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
; implied. See the License for the specific language governing
; permissions and limitations under the License.
;
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
