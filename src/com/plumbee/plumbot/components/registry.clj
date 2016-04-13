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
(ns com.plumbee.plumbot.components.registry
  (:require [clojure.core.async :refer [chan >!! thread alts!! close!]]
            [com.stuartsierra.component :as c]
            [com.plumbee.plumbot.components.websocket :refer [add-listener]]
            [com.plumbee.plumbot.components.webapi :refer [chat-postMessage chat-update im-open channels-history]]
            [com.plumbee.plumbot.components.timing :refer [set-interval seconds]]
            [com.plumbee.plumbot.support.logging :as log]
            [com.plumbee.plumbot.support.cron :refer [time-now should-run]]))


(defn- process-outbox [{outbox :outbox :as state} {webapi :webapi} persona]
  (log/info "Processing outbox...")
  (doseq [item outbox]
    (try
      (cond
        (= (:type item) :message) (chat-postMessage webapi
                                                    (:channel-id item)
                                                    (:text item)
                                                    (merge persona (:params item)))

        (= (:type item) :direct) (chat-postMessage webapi
                                                   (:id (:channel (im-open webapi (:user-id item))))
                                                   (:text item)
                                                   (merge persona (:params item)))

        (= (:type item) :update) (chat-update webapi
                                              (:timestamp item)
                                              (:channel-id item)
                                              (:text item))

        :else (log/error "Unknown :outbox item: " item))

      (catch Exception e
        (log/error "Unable to process :outbox item: " item (.getMessage e)))))
  (dissoc state :outbox))

(defn pre-process-input [{webapi :webapi} {type :type {reacted-msg-ts :ts reacted-msg-channel :channel} :item :as event}]
  (if (contains? #{"reaction_added" "reaction_removed"} type)
    (let [message (-> (channels-history webapi reacted-msg-channel reacted-msg-ts)
                      :messages
                      first)]
      (assoc event :reacted message))
    event))

(defn- handle-event [registry event]
  (let [local-event (pre-process-input registry event)]
    (doseq [{:keys [handler state persona]} (vals @(:bot-config registry))]
      (log/swallow
        (log/context (str "Bot: <" (:username persona) "> Handler: <" handler "> Event: <" local-event ">")
                     (swap! state handler local-event)
                     (swap! state process-outbox registry persona))))))

(defn push-cron-event [channel last-time]
  (let [now (time-now)]
    (when (not= now @last-time)
       (reset! last-time now)
       (>!! channel {:cron now}))))

(defn handle-cron [registry cron-now]
  (doseq [{handler :handler state :state persona :persona} (vals @(:bot-config registry))]
    (doseq [[key cron-str] (:cron @state)]
      (log/swallow
        (log/context (str "Bot: <" (:username persona) "> Handler: <" handler "> Now: <" cron-now "> Cron: <" cron-str ">")
          (let [cron-expr (.split cron-str "\\s+")
                event {:type key}]
            (log/context (str "Event: <" event ">")
              (when (should-run cron-expr cron-now)
                (swap! state handler event)
                (swap! state process-outbox registry persona)))))))))

(defn handle-channel-event [registry {cron-now :cron :as event}]
  (if cron-now
    (handle-cron registry cron-now)
    (handle-event registry event)))

(defrecord Registry [bot-config timer webapi websocket]

  c/Lifecycle

  (start [component]
    (log/always \^ (.getSimpleName (type component)))

    (let [cron-channel (chan 30)
          last-time (atom nil)
          websocket-channel (chan 100)]

      (thread (loop []
                (let [[event _] (alts!! [cron-channel websocket-channel])]
                  (when event
                    (handle-channel-event component event)
                    (recur)))))

      (>!! cron-channel {:type :startup})
      (set-interval timer #(push-cron-event cron-channel last-time) (* 30 seconds))
      (add-listener websocket #(>!! websocket-channel %))

      (assoc component :channels [cron-channel websocket-channel])))

  (stop [component]
    (log/always \_ (.getSimpleName (type component)))

    (let [[cron-channel websocket-channel] (:channels component)]
      (>!! cron-channel {:type :shutdown})
      (close! cron-channel)
      (close! websocket-channel))

    component))
