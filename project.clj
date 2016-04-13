(defproject com.plumbee/plumbot "0.0.1"
  :description "A Clojure library for hosting Slack bots."
  :url "http://plumbee.com"
  :license {:name "Apache License"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/core.async "0.2.374"]
                 [com.stuartsierra/component "0.3.1"]
                 [org.slf4j/slf4j-api "1.7.21"]
                 [org.eclipse.jetty.websocket/websocket-client "9.3.0.M2"]
                 [ring/ring-codec "1.0.0"]]
  :profiles {:dev {:resource-paths ["test-resources"]
                   :jvm-opts ["-Dstate.path=test-resources/"]}})
