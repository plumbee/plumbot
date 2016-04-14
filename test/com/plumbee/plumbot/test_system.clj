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
(ns com.plumbee.plumbot.test-system
  (:require [clojure.test :refer :all]
            [clojure.core.async :as a]
            [com.stuartsierra.component :as c]
            [com.plumbee.plumbot.system :refer [plumbot-system]]
            [com.plumbee.plumbot.register :refer [register-bot! ->Bot ->Persona]]
            [com.plumbee.plumbot.components.timing :refer [->Timer]]

            [com.plumbee.plumbot.components.debug :refer [map->Debug]]
            [com.plumbee.plumbot.components.websocket :refer [map->WebSocket]]
            [com.plumbee.plumbot.components.webapi :refer [map->WebApi call]]
            [com.plumbee.plumbot.components.registry :refer [map->Registry]])
  (:import (java.util.concurrent TimeoutException)))




(def slack-config {:slack-api-token "Test Token"
                   :slack-api-url "http://test.url"})

(def mock-websocket {:timer nil
                     :webapi nil
                     :listeners (atom [])})

(def system-map (agent (c/system-map
                         :bot-config com.plumbee.plumbot.register/bot-config
                         :timer (->Timer)
                         :webapi (map->WebApi slack-config)
                         :websocket mock-websocket
                         :registry (c/using (map->Registry {}) [:bot-config :timer :webapi :websocket])
                         :debug (c/using (map->Debug {}) [:webapi]))))


(register-bot! (->Bot (->Persona "EchoBot" ":+1:")
                      ["?"]
                      (fn [state {:keys [channel type text]}]
                        (or (when (and text (= type "message"))
                              (update-in state [:outbox] conj {:type       :message
                                                               :channel-id channel
                                                               :text       (str "Echo: " text)}))
                            state))
                      (atom nil)))


(def api-call-input (a/chan 10))
(def api-call-output (a/chan 10))

(defn mock-call [{:keys [slack-api-url slack-api-token]} api-method-name params]

  (assert (= slack-api-token "Test Token"))
  (assert (= slack-api-url "http://test.url"))

  (a/>!! api-call-input {:api-method-name api-method-name :params params})

  (let [[v c] (a/alts!! [api-call-output (a/timeout 5000)])]
    (if (= c api-call-output)
      v
      (throw (TimeoutException. "Waited but no call response was available.")))))

(defn enqueue-api-response [response]
  (a/>!! api-call-output response))

(defn dequeue-api-request []
  (let [[v c] (a/alts!! [api-call-input (a/timeout 5000)])]
    (if (= c api-call-input)
      v
      (throw (TimeoutException. "Waited but no call request was made.")))))


(defn send-over-websocket [event]
  (doseq [listener @(:listeners mock-websocket)]
    (listener event)))


(deftest System-Test
  (testing "integration of system"
    (binding [call mock-call]
      (send system-map c/start)

      (enqueue-api-response {:ok true})
      (is (= (:api-method-name (dequeue-api-request)) "users.setPresence"))

      (send-over-websocket {:type "message" :text "hello" :channel "CBeebies"})

      (enqueue-api-response {:ok true})
      (let [request (dequeue-api-request)]
        (is (= (:api-method-name request) "chat.postMessage"))
        (is (= (:channel (:params request)) "CBeebies"))
        (is (= (:text (:params request)) "Echo: hello")))

      (send system-map c/stop)

      (enqueue-api-response {:ok true})
      (is (= (:api-method-name (dequeue-api-request)) "users.setPresence"))

      )))
