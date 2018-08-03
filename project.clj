(defproject clj-vxe "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [io.forward/yaml "1.0.8"]
                 [org.clojure/data.xml "0.0.8"]
                 [clj-http "3.8.0"]
                 [com.cemerick/pomegranate "0.4.0"]
                 [clojure.java-time "0.3.1"]
                 [org.clojure/tools.logging "0.2.4"]
                 [org.slf4j/slf4j-log4j12 "1.7.1"]
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail
                                                    javax.jms/jms
                                                    com.sun.jmdk/jmxtools
                                                    com.sun.jmx/jmxri]]]
  :main ^:skip-aot clj-vxe.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
