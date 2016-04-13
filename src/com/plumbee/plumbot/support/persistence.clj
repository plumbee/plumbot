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
(ns com.plumbee.plumbot.support.persistence
  (:import [java.lang.RuntimeException])
  (:require [clojure.edn :as edn]
            [clojure.pprint :as pprint]))


(defn- try-read
  "Attempt to read a file. Swallow exceptions and use the given supplier to provide a
  value in case of failure. If the supplier is used, the supplied value will be written
  to the file."
  [filename supplier]
  (try
    (edn/read-string (slurp filename))
    (catch Exception _
      (let [fallback (supplier)]
        (spit filename (prn-str fallback))
        fallback))))

(defn- persist-state
  "Write the new-state to file."
  [file-name _ _ _ new-state]
  (spit file-name (with-out-str (pprint/pprint new-state))))

(def state-path
  (System/getProperty "state.path" ""))

(defn- file-namify
  "Generate a filename from a unique key."
  [key]
  (str state-path (name key) ".edn"))

(defn persistent-atom
  "Create an atomic reference to a value from a file-based key value store.
  On changing the atom, the file-based store will also be updated.
  If the file-based store currently has no value it will be initialised
  with the result of calling the supplier."
  [key supplier]
  (let [file-name (file-namify key)
        state (atom (try-read file-name supplier))]
    (add-watch state :persistor (partial persist-state file-name))))

(defn load-data
  "Load a value from a file-based key value store.
  If the file-based store currently has no value it will be initialised
  with the result of calling the supplier."
  [key supplier]
  (let [file-name (file-namify key)]
    (try-read file-name supplier)))
