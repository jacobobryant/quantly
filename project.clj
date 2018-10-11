; TODO
; make run-dev work
; communicate with sente
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
                 ]

  :plugins [[lein-cljsbuild "1.1.7"]]

  :min-lein-version "2.0.0"
  :resource-paths ["config" "resources" "target/cljs-out"]
  :source-paths ["src/clj" "src/cljc" "src/cljs"]

  :clean-targets ^{:protect false}
  [:target-path
   [:cljsbuild :builds :app :compiler :output-dir]
   [:cljsbuild :builds :app :compiler :output-to]]

  :aliases
  {"fig" ["trampoline" "run" "-m" "figwheel.main" "-b" "quantly" "-r"]
   "run-dev" ["trampoline" "run" "-m" "quantly.server/run-dev"]}

  :cljsbuild
  {:builds [{:source-paths ["src/cljs" "src/cljc"]
             :compiler
             {:output-to        "target/cljs-out/public/quantly-main.js"
              :output-dir       "target/cljs-out/public/"
              :source-map       "target/cljs-out/public/quantly-main.js.map"
              :optimizations :advanced
              :pretty-print  false}}]}

  :main quantly.server

  :profiles
  {:dev {:dependencies [[com.bhauman/figwheel-main "0.1.9"]
                        [com.bhauman/rebel-readline-cljs "0.1.4"]]}
   :uberjar {:prep-tasks ["compile" ["cljsbuild" "once"]]
             :env {:production true}
             :aot :all
             :omit-source true}})
