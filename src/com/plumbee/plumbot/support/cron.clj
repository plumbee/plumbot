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

