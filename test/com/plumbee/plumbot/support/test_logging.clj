(ns com.plumbee.plumbot.support.test-logging
  (:require [clojure.test :refer :all]
            [com.plumbee.plumbot.support.logging :refer :all]))


(deftest Logging
  (testing "context"
    (let [caught (try (context "Fruit Bowl"
                               (throw (IllegalArgumentException. "Banana")))
                      (catch Throwable t t))]
      (is (.contains (.getMessage caught) "Fruit Bowl"))
      (is (.contains (.getMessage (.getCause caught)) "Banana"))
      (is (instance? IllegalArgumentException (.getCause caught))))))
