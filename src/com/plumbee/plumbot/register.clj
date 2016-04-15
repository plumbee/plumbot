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
(ns com.plumbee.plumbot.register)


(def help-text (atom "No bots registered."))
(def help-text-by-bot (atom {}))

(defn- gen-help [_ _ _ config]
  (reset! help-text (str "Available bots:\n"
                         (apply str (map (comp #(str "    `" % "`\n") :username :persona) (vals config)))
                         "Say a bot's name to see the available commands for a bot."))
  (reset! help-text-by-bot (into {} (for [bot (vals config)] [(:username (:persona bot)) (:help-text bot)]))))

(def bot-config (add-watch (atom {}) :gen-help gen-help))

(defn- make-help-handler [trigger-words]
  (let [triggers (into #{} (map #(.toLowerCase %) trigger-words))]
    (fn [state {:keys [text channel]}]
      (cond
        (and text (triggers (.toLowerCase text))) {:outbox [{:type       :message
                                                  :channel-id channel
                                                  :text       @help-text}]}
        (@help-text-by-bot text) {:outbox [{:type       :message
                                            :channel-id channel
                                            :text       (apply str (interpose "\n" (@help-text-by-bot text)))}]}
        :else state))))

(defrecord Persona [username icon_emoji])
(defrecord Bot [persona help-text handler state])

(defn help-bot [& trigger-words]
  (->Bot (->Persona "HelpBot" ":information_source:")
         ["Say a bot's name to see the available commands for a bot."]
         (make-help-handler (cons "HelpBot" trigger-words))
         (atom nil)))

(defn register-bot! [bot]
  (let [key (-> bot :persona :username)]
    (swap! bot-config assoc key bot)))
