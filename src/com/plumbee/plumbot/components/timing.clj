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
(ns com.plumbee.plumbot.components.timing
  (:require [com.stuartsierra.component :as c]
            [com.plumbee.plumbot.support.logging :as log]))


(def seconds 1000)
(def minutes (* 60 seconds))
(def hours (* 60 minutes))
(def days (* 24 hours))

(defn set-interval
  "Run the given callback every ms milliseconds."
  [component callback ms]
  (future
    (while @(:live component)
      (try
        (callback)
        (catch Exception e (.printStackTrace e)))
      (Thread/sleep ms))))

(defn set-timeout
  "Run the given callback after ms milliseconds."
  [component callback ms]
  (future
    (Thread/sleep ms)
    (if @(:live component)
      (try
        (callback)
        (catch Exception e (.printStackTrace e))))))

(defrecord Timer []

  c/Lifecycle

  (start [component]
    (log/always \^ (.getSimpleName (type component)))
    (assoc component :live (atom true)))

  (stop [component]
    (log/always \_ (.getSimpleName (type component)))
    (reset! (:live component) false)
    component))
