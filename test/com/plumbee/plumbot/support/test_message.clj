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
(ns com.plumbee.plumbot.support.test-message
  (:require [clojure.test :refer :all]
            [com.plumbee.plumbot.support.message :refer :all]))


(defn make-tones [emoji]
  (into #{} (map str
                 (repeat emoji)
                 [":skin-tone-2:" ":skin-tone-3:" ":skin-tone-4:" ":skin-tone-5:" ":skin-tone-6:"])))

(deftest Message
  (testing "non-blank?"
    (is (non-blank? "a"))
    (is (not (non-blank? "")))
    (is (not (non-blank? nil)))
    (is (not (non-blank? " ")))
    (is (not (non-blank? "\t"))))
  (testing "slack-quote"
    (is (= (slack-quote "A") "```A```"))
    (is (= (slack-quote "A" "b" "c.") "```A\nb\nc.```")))
  (testing "join-seq"
    (is (= (join-seq "-" []) ""))
    (is (= (join-seq "-" [nil]) ""))
    (is (= (join-seq "-" [nil nil nil]) ""))
    (is (= (join-seq "-" ["a"]) "a"))
    (is (= (join-seq "-" ["a" "b" "c"]) "a-b-c"))
    (is (= (join-seq "-" ["a" nil "b" " " "\n" "c" "\t"]) "a-b-c")))
  (testing "skin-tone"
    (is ((make-tones ":+1:") (skin-tone ":+1:"))))
  (testing "de-link"
    (is (= "www.hello.com" (de-link "<hello|www.hello.com>")))
    (is (= "www.hello.com" (de-link "<|www.hello.com>")))
    (is (= "www.hello.com" (de-link "www.hello.com")))))