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
(ns com.plumbee.plumbot.components.webapi
  (:require [clojure.data.json :as json]
            [com.stuartsierra.component :as c]
            [com.plumbee.plumbot.support.logging :as log]
            [ring.util.codec :refer [url-encode]]))


(defrecord WebApi [slack-api-url slack-api-token]

  c/Lifecycle

  (start [component]
    (log/always \^ (.getSimpleName (type component)))
    component)

  (stop [component]
    (log/always \_ (.getSimpleName (type component)))
    component))

(defn- json-encode [x]
  (if (= String (type x))
    x
    (json/write-str x)))

(defn- querify
  "Turn a Clojure map into a query string of the form 'key1=value1&key2=value2...'"
  [params]
  (apply str
         (interpose "&"
                    (for [[k v] params]
                      (str (url-encode (name k))
                           "="
                           (url-encode (json-encode v)))))))


;TODO: if "ok": false then log/error
(defn call
  "Make an http call to slack."
  [{:keys [slack-api-url slack-api-token]} api-method-name params]
  (log/info "Calling: " api-method-name params slack-api-url slack-api-token)
  (let [hierarchy (str slack-api-url api-method-name "?")
        query (querify (assoc params "token" slack-api-token))
        uri (str hierarchy query)]
    (log/info "Calling: " uri)
    (json/read-str (slurp uri) :key-fn keyword)))

;-------------------- Functions to call slack Web API. https://api.slack.com/web --------------------;

; https://api.slack.com/methods/rtm.start
(defn rtm-start [webapi]
  (call webapi "rtm.start" {}))

; https://api.slack.com/methods/im.open
(defn im-open [webapi user-id]
  (call webapi "im.open" {:user user-id}))

; https://api.slack.com/methods/chat.postMessage
(defn chat-postMessage
  ([webapi channel-id text]
   (call webapi "chat.postMessage" {:channel channel-id
                                    :text    text
                                    :as_user true}))
  ([webapi channel-id text params]
   (call webapi "chat.postMessage" (merge {:channel  channel-id
                                           :text     text
                                           :as_user  false} params))))

; https://api.slack.com/methods/chat.update
(defn chat-update
  [webapi timestamp channel-id text]
  (call webapi "chat.update" {:ts timestamp
                              :channel channel-id
                              :text text}))

; https://api.slack.com/methods/channels.history
(defn channels-history
  [webapi channel-id timestamp]
  (call webapi "channels.history" {:channel channel-id
                                  :inclusive 1
                                  :latest timestamp
                                  :oldest timestamp}))

;https://api.slack.com/methods/users.setPresence
(defn users-setPresence
  [webapi status]
  (call webapi "users.setPresence" {:presence status}))
