(ns com.plumbee.plumbot.register)


(def help-text (atom "No bots registered."))
(def help-text-by-bot (atom {}))

(defn gen-help [_ _ _ config]
  (reset! help-text (str "Available bots:\n"
                         (apply str (map (comp #(str "    `" % "`\n") :username :persona) (vals config)))
                         "Say a bot's name to see the available commands for a bot."))
  (reset! help-text-by-bot (into {} (for [bot (vals config)] [(:username (:persona bot)) (:help-text bot)]))))

(def bot-config (add-watch (atom {}) :gen-help gen-help))

(defn- make-help-handler [trigger-words]
  (let [triggers (into #{} (map #(.toLowerCase %) trigger-words))]
    (fn [state {:keys [text channel]}]
      (cond
        (triggers (.toLowerCase text)) {:outbox [{:type       :message
                                                  :channel-id channel
                                                  :text       @help-text}]}
        (@help-text-by-bot text) {:outbox [{:type       :message
                                            :channel-id channel
                                            :text       (apply str (interpose "\n" (@help-text-by-bot text)))}]}
        :else state))))

(defn- add-bot [bot-config name icon-emoji help-text handler-fn state-atom]
  (assoc bot-config name
                    {:persona   {:username name, :icon_emoji icon-emoji}
                     :help-text help-text
                     :handler   handler-fn
                     :state     state-atom}))

(defmacro defbot [bot-name icon-emoji help-texts handler-fn state-atom]
  '(swap! bot-config
          add-bot (str bot-name) icon-emoji help-texts handler-fn state-atom))

(defn initialise-help-bot [& trigger-words]
  (swap! bot-config
         add-bot "HelpBot" ":information_source:"
         ["Say a bot's name to see the available commands for a bot."]
         (make-help-handler (cons "HelpBot" trigger-words))
         (atom nil)))
