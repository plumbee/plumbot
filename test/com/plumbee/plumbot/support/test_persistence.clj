(ns com.plumbee.plumbot.support.test-persistence
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [com.plumbee.plumbot.support.persistence :refer :all]
            [clojure.edn :as edn]))


(defn fixture [test-fn]
  (spit "test-resources/data01.edn" (prn-str [1 2 3]))
  (io/delete-file "test-resources/data02.edn" true)
  (io/delete-file "test-resources/data03.edn" true)
  (test-fn)
  (io/delete-file "test-resources/data02.edn" true)
  (io/delete-file "test-resources/data03.edn" true))

(use-fixtures :each fixture)

(deftest Persistence
  (testing "load-data"
    (is (= [1 2 3] (load-data :data01 (constantly [1 2 3]))))
    (is (= [1 2 3] (load-data :data02 (constantly [1 2 3])))))
  (testing "persistent-atom"
    (let [test-atom (persistent-atom :data03 (constantly {:a 1 :b 2}))
          data-after-load @test-atom
          data-after-change (swap! test-atom update :b inc)
          file-after-change (edn/read-string (slurp "test-resources/data03.edn"))]
      (is (= data-after-load {:a 1 :b 2}))
      (is (= data-after-change {:a 1 :b 3}))
      (is (= file-after-change {:a 1 :b 3})))))
