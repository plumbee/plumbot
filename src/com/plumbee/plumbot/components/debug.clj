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
