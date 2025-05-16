(ns com.eelchat-test
  (:require [clojure.test :refer [deftest is]]
            [com.eelchat :as main]
            [xtdb.api :as xt]))

(deftest example-test
  (is (= 4 (+ 2 2))))

(defn get-context [node]
  {:biff.xtdb/node  node
   :biff/db         (xt/db node)
   :biff/malli-opts #'main/malli-opts})
