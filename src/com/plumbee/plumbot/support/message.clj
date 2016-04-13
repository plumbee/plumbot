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

(defn de-link [str]
  (if-let [groups (re-find #"<.*\|(.*)>" str)]
    (nth groups 1)
    str))