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
