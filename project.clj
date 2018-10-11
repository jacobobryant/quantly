(defproject quantly "0.0.1-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [io.pedestal/pedestal.service "0.5.4"]
                 [io.pedestal/pedestal.jetty "0.5.4"]
                 [ch.qos.logback/logback-classic "1.2.3"
                  :exclusions [org.slf4j/slf4j-api]]
                 [org.slf4j/jul-to-slf4j "1.7.25"]
                 [org.slf4j/jcl-over-slf4j "1.7.25"]
                 [org.slf4j/log4j-over-slf4j "1.7.25"]
                 [com.datomic/datomic-pro "0.9.5703"
                  :exclusions [com.google.guava/guava]]
                 [hiccup "1.0.5"]
                 [org.clojure/clojurescript "1.10.339"
                  :scope "provided"]
                 [com.bhauman/figwheel-main "0.1.9"]
                 [com.bhauman/rebel-readline-cljs "0.1.4"]]

  :min-lein-version "2.0.0"
  :resource-paths ["config" "resources" "target/cljs-out"]
  :source-paths ["src/clj" "src/cljc" "src/cljs"]

  :clean-targets ^{:protect false}
  [:target-path]

  :aliases
  {"fig" ["trampoline" "run" "-m" "figwheel.main" "-b" "quantly" "-r"]
   "js" ["trampoline" "run" "-m" "figwheel.main" "-O" "advanced" "-bo" "quantly"]
   "run-dev" ["trampoline" "run" "-m" "quantly.server/run-dev"]}

  :main quantly.server

  :profiles
  {:uberjar {:prep-tasks ["compile" ["js"]]
             :env {:production true}
             :aot :all
             :omit-source true}})
