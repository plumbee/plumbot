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
(ns com.plumbee.plumbot.support.test-cron
  (:require [clojure.test :refer :all]
            [com.plumbee.plumbot.support.cron :refer :all]))


(deftest Cron
  (testing "time-now"
    (is (= 5 (count (time-now)))))
  (testing "should-run 8am til 6pm every weekday"
    (is (not (should-run ["*" "8-17" "*" "*" "1-5"] [59 7 25 12 3])))
    (is (should-run ["*" "8-17" "*" "*" "1-5"] [0 8 25 12 3]))
    (is (should-run ["*" "8-17" "*" "*" "1-5"] [30 13 25 12 3]))
    (is (should-run ["*" "8-17" "*" "*" "1-5"] [59 17 25 12 3]))
    (is (not (should-run ["*" "8-17" "*" "*" "1-5"] [0 18 25 12 3]))))
  (testing "should-run on specific minute"
    (is (not (should-run ["1" "*" "*" "*" "*"] [2 1 1 1 1])))
    (is (should-run ["1" "*" "*" "*" "*"] [1 1 1 1 1])))
  (testing "should-run on minute range"
    (is (not (should-run ["1-5" "*" "*" "*" "*"] [0 1 1 1 1])))
    (is (not (should-run ["1-5" "*" "*" "*" "*"] [6 1 1 1 1])))
    (is (should-run ["1-5" "*" "*" "*" "*"] [1 1 1 1 1]))
    (is (should-run ["1-5" "*" "*" "*" "*"] [5 1 1 1 1])))
  (testing "should-run on minute combo"
    (is (not (should-run ["1-5,17" "*" "*" "*" "*"] [0 1 1 1 1])))
    (is (not (should-run ["1-5,17" "*" "*" "*" "*"] [6 1 1 1 1])))
    (is (not (should-run ["1-5,17" "*" "*" "*" "*"] [16 1 1 1 1])))
    (is (not (should-run ["1-5,17" "*" "*" "*" "*"] [18 1 1 1 1])))
    (is (should-run ["1-5,17" "*" "*" "*" "*"] [1 1 1 1 1]))
    (is (should-run ["1-5,17" "*" "*" "*" "*"] [5 1 1 1 1]))
    (is (should-run ["1-5,17" "*" "*" "*" "*"] [17 1 1 1 1])))
  (testing "should-run on specific hour"
    (is (not (should-run ["*" "5" "*" "*" "*"] [1 1 1 1 1])))
    (is (should-run ["*" "5" "*" "*" "*"] [1 5 1 1 1])))
  (testing "should-run on specific day of the month"
    (is (not (should-run ["*" "*" "22" "*" "*"] [1 1 1 1 1])))
    (is (should-run ["*" "*" "22" "*" "*"] [1 1 22 1 1])))
  (testing "should-run on specific month"
    (is (not (should-run ["*" "*" "*" "12" "*"] [1 1 1 1 1])))
    (is (should-run ["*" "*" "*" "12" "*"] [1 1 1 12 1])))
  (testing "should-run on specific day of the week"
    (is (not (should-run ["*" "*" "*" "*" "3"] [1 1 1 1 1])))
    (is (should-run ["*" "*" "*" "*" "3"] [1 1 1 1 3]))))

