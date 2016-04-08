(ns com.plumbee.plumbot.support.filter
  "Possibly useful functions for filtering and handling incoming slack messages.")


(defn of-type
  "Wrap an event handler in a type filter. The given handler will only be called if the event type matches."
  [type handler]
  (fn [state event]
    (if (= (:type event) type)
      (handler state event)
      state)))

(defn text-matches
  "Wrap an event handler in a regex filter. The given handler will only be called if the event text matches
  the regex. The matching groups from the regex will be added to the message passed to the handler."
  [regex handler]
  (fn [state event]
    (if-let [text (:text event)]
      (if-let [groups (re-find regex text)]
        (handler state (assoc event :regex-groups groups))
        state)
      state)))

(defn in-channel
  "Wrap an event handler in a channel filter. The given handler will only be called if the event channel matches."
  [channel-id handler]
  (fn [state event]
    (if (= (:channel event) channel-id)
      (handler state event)
      state)))

(defn cond-re
  "Takes a String and a number of regex function pairs.
  For the first regex that matches (using re-find), the matching groups are passed to the respective function.
  Returns nil if no regex matches the text."
  [text & [re function & more]]
  (if-let [groups (re-find re text)]
    (function groups)
    (when more (recur text more))))
