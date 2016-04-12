(ns com.plumbee.plumbot.support.test-filter
  (:require [clojure.test :refer :all]
            [com.plumbee.plumbot.support.filter :refer :all]))


(defn payload-handler [state event]
  (assoc state :result (:payload event)))

(defn regex-handler [state event]
  (assoc state :result (:regex-groups event)))

(deftest Filter
  (testing "of-type"
    (let [decorated-handler (of-type "banana" payload-handler)]
      (is (= (decorated-handler {:old "state"} {:type "banana" :payload 5})
             {:old "state" :result 5}))
      (is (= (decorated-handler {:old "state"} {:type "apple" :payload 5})
             {:old "state"}))))
  (testing "in-channel"
    (let [decorated-handler (in-channel "tunnel" payload-handler)]
      (is (= (decorated-handler {:old "state"} {:channel "tunnel" :payload 5})
             {:old "state" :result 5}))
      (is (= (decorated-handler {:old "state"} {:channel "BBC 1" :payload 5})
             {:old "state"}))))
  (testing "text-matches"
    (let [decorated-handler (text-matches #"([Bb]+)ob" regex-handler)]
      (is (= (decorated-handler {:old "state"} {:text "Bbbbob" :payload 5})
             {:old "state" :result ["Bbbbob" "Bbbb"]}))
      (is (= (decorated-handler {:old "state"} {:text "oboe" :payload 5})
             {:old "state"}))))
  (testing "cond-re"
    (is (= (cond-re "Hello World"
                    #"abc" :doesn't-match
                    #"Hello (.*)" second)
           "World"))
    (is (= (cond-re "Hello World"
                    #"abc" :doesn't-match
                    #"Hello Banana" second)
           nil))))
