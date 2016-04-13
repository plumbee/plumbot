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
(ns com.plumbee.plumbot.components.websocket
  (:require [com.stuartsierra.component :as c]
            [clojure.data.json :as json]
            [com.plumbee.plumbot.support.logging :as log]
            [com.plumbee.plumbot.components.webapi :refer [rtm-start]]
            [com.plumbee.plumbot.components.timing :refer [set-interval seconds]])
  (:import (org.eclipse.jetty.util.ssl SslContextFactory)
           (org.eclipse.jetty.websocket.client WebSocketClient ClientUpgradeRequest)
           (org.eclipse.jetty.websocket.api WebSocketListener StatusCode)
           (java.net URI)))


(defn- connect
  "Create a websocket connection."
  [webapi connection listeners]
  (let [slack-response (rtm-start webapi)
        uri (URI. (slack-response :url))
        handler (fn [message] (doseq [listener @listeners]
                        (listener message)))
        client (doto (WebSocketClient. (SslContextFactory.)) .start)
        listener (reify WebSocketListener
                   (onWebSocketBinary [_ bytes int1 int2]
                     (log/trace "Binary: " bytes int1 int2))
                   (onWebSocketClose [_ code message]
                     (log/info "Close: " code message)
                     (reset! connection nil))
                   (onWebSocketConnect [_ _]
                     (log/info "Connected."))
                   (onWebSocketError [_ throwable]
                     (log/warn "Error: " throwable))
                   (onWebSocketText [_ message]
                     (let [event (json/read-str message :key-fn keyword)]
                       (log/trace "Event: " event)
                       (handler event))))
        session (.get (.connect client listener uri (ClientUpgradeRequest.)))]
    (reset! connection [client session])))

(defn- disconnect
  "Close the websocket connection."
  [[client session]]
  (when (and client session)
    (try (.close session StatusCode/NORMAL "Good bye.") (catch Exception e (println e)))
    (try (.stop client) (catch Exception e (println e)))))

(defn- ping
  "Ping the slack server over the websocket to stop the connection timing out."
  [webapi connection listeners send-id]
  (if (not @connection)
    (connect webapi connection listeners))
  (let [[_ session] @connection
        message (json/write-str {:type "ping"
                                 :id   (swap! send-id inc)})]
    (.sendStringByFuture (.getRemote session) message)))


(defrecord WebSocket [timer webapi]

  c/Lifecycle

  (start [component]
    (log/always \^ (.getSimpleName (type component)))
    (let [listeners (atom [])
          connection (atom nil)
          send-id (atom 0)]
      (set-interval timer
                    #(ping webapi connection listeners send-id)
                    (* 30 seconds))
      (assoc component :listeners listeners
                       :connection connection)))

  (stop [component]
    (log/always \_ (.getSimpleName (type component)))
    (disconnect @(:connection component))
    component))

(defn add-listener [websocket listener]
  (swap! (:listeners websocket) conj listener))
