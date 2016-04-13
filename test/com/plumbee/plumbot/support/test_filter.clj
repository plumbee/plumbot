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
