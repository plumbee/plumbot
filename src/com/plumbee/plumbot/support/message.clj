(ns com.plumbee.plumbot.support.message
  "Possibly useful functions for formatting messages."
  (:require [clojure.string :refer [blank?]]))


(def non-blank? (complement blank?))

(defn slack-quote [& lines]
  (apply str (concat ["```"] (interpose "\n" lines) ["```"])))

(defn join-seq [sep seq]
  (apply str (interpose sep (filter non-blank? seq))))

(def skin-tones (map #(str ":skin-tone-" % ":") (range 2 7)))

(defn skin-tone
  ([] (rand-nth skin-tones))
  ([emoji] (str emoji (rand-nth skin-tones))))

(defn pl [count singular plural]
  (if (= count 1)
    singular
    plural))

(defn de-link [str]
  (if-let [groups (re-find #"<.*\|(.*)>" str)]
    (nth groups 1)
    str))