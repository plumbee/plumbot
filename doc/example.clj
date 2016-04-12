(ns com.plumbee.plumbot.example
  (:require [com.stuartsierra.component :as c]
            [com.plumbee.plumbot.system :refer [plumbot-system]]
            [com.plumbee.plumbot.register :refer [register-bot! help-bot ->Bot ->Persona]]))


(def dice-bot (->Bot (->Persona "DiceBot" ":game_die:")
                     ["Say `roll d<n>` to roll an n-sided die."]
                     (fn [state {:keys [channel type text]}]
                       (or (when (and text (= type "message"))
                             (when-let [groups (re-find #"roll d(\d+)" text)]
                               (let [total (Integer/parseInt (groups 1))
                                     text (str "You rolled: " (inc (rand-int total)))]
                                 (update-in state [:outbox] conj {:type       :message
                                                                  :channel-id channel
                                                                  :text       text}))))
                           state))
                     (atom nil))) ; This bot is stateless so we pass an empty atom.

(register-bot! (help-bot "plumbot" "help me!"))
(register-bot! dice-bot)

; But of course you'll want a reference to the system map so you can c/stop it later!
(c/start (plumbot-system {:slack-api-token   "<Insert token here>"
                          :slack-api-url     "https://slack.com/api/"}))
