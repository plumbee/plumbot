(ns com.plumbee.plumbot.support.logging
  "Wrapper around slf4j logging."
  (:require [com.plumbee.plumbot.support.persistence :refer [load-data]])
  (:import (org.slf4j LoggerFactory)))


(def loggers (atom {}))

(def enabled? {:fatal  (constantly true)
               :error  #(.isErrorEnabled %)
               :warn   #(.isWarnEnabled %)
               :info   #(.isInfoEnabled %)
               :debug  #(.isDebugEnabled %)
               :trace  #(.isTraceEnabled %)
               :always (constantly true)})

(defn get-logger [namespace]
  (let [key (keyword (ns-name namespace))]
    (if-let [logger (key @loggers)]
      logger
      (let [new-logger (LoggerFactory/getLogger (str namespace))]
        (swap! loggers assoc key new-logger)
        new-logger))))

(defn log? [namespace level]
  (let [logger (get-logger namespace)]
    ((level enabled?) logger)))

(defn log [level namespace & args]
  (let [logger (get-logger namespace)
        message (apply str (interpose \space args))]
    (case level
      :error (.error logger message)
      :warn (.warn logger message)
      :info (.info logger message)
      :debug (.debug logger message)
      :trace (.trace logger message)
      :always (println (.getName logger) ">>>" message)))
  (if (= :error level)
    (doseq [arg args :when (instance? Throwable arg)] (.printStackTrace arg))))


(defmacro error [& args]
  (when (log? *ns* :error)
    `(log :error ~*ns* ~@args)))

(defmacro warn [& args]
  (when (log? *ns* :warn)
    `(log :warn ~*ns* ~@args)))

(defmacro info [& args]
  (when (log? *ns* :info)
    `(log :info ~*ns* ~@args)))

(defmacro debug [& args]
  (when (log? *ns* :debug)
    `(log :debug ~*ns* ~@args)))

(defmacro trace [& args]
  (when (log? *ns* :trace)
    `(log :trace ~*ns* ~@args)))

(defmacro always [& args]
  `(log :always ~*ns* ~@args))

(defmacro context [text & body]
  `(try
     ~@body
     (catch Exception e# (throw (RuntimeException. ~text e#)))))

(defmacro swallow [& body]
  `(try
     ~@body
     (catch Exception e# (error e#))))
