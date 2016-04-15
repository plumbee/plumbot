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
(ns com.plumbee.plumbot.support.cron
  (:import (java.time LocalDateTime)))


(defn time-now []
  (let [now (LocalDateTime/now)
        minute (.getMinute now)
        hour (.getHour now)
        dayOfMonth (.getDayOfMonth now)
        month (.getValue (.getMonth now))
        dayOfWeek (mod (.getValue (.getDayOfWeek now)) 7)]
    [minute hour dayOfMonth month dayOfWeek]))

(defn- to-int [string]
  (Integer/parseInt string))

(defn- matches [expr actual]
  (cond
    (= expr "*") true
    (.contains expr ",") (some identity (map #(matches % actual) (.split expr ",")))
    (.contains expr "-") (let [[lower upper] (map to-int (.split expr "-"))]
                           (<= lower actual upper))
    :else (= (to-int expr) actual)))

(defn should-run [[minute-x hour-x dayOfMonth-x month-x dayOfWeek-x]
                  [minute hour dayOfMonth month dayOfWeek]]
  (and (matches minute-x minute)
       (matches hour-x hour)
       (matches dayOfMonth-x dayOfMonth)
       (matches month-x month)
       (matches dayOfWeek-x dayOfWeek)))

