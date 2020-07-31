(def MAIN 'project.app.main)
(prn (clojure-version))

(defproject app "0.1.0"

  :repositories [["central" {:url "https://repo1.maven.org/maven2/"}]
                 ["clojars" {:url "https://clojars.org/repo/"}]
                 ["conjars" {:url "https://conjars.org/repo"}]]

  :min-lein-version "2.9.4"

  :plugins [[org.clojure/tools.deps.alpha "0.8.677"]
            [lein-tools-deps "0.4.5" :exclusions [org.clojure/tools.deps.alpha]]
            [io.taylorwood/lein-native-image "0.3.1"]]
  :middleware [lein-tools-deps.plugin/resolve-dependencies-with-deps-edn]

  :lein-tools-deps/config {:config-files [:install :user :project]}

  :repl-options {:init-ns          ~MAIN
                 :main             ~MAIN
                 :init (-main)
                 :host             "0.0.0.0"
                 :port             7788}
  :profiles {:dev  {:main         ^{:skip-aot false} ~MAIN
                    :aot          nil #_[datastore.serdes]
                    :aliases      {"dev" ["trampoline" "run" "-m" (format "%s/-main" ~MAIN)]}
                    :dependencies []}

             :repl {:plugins [[nrepl/nrepl "0.6.0"]
                              [cider/cider-nrepl "0.24.0"]]}

             :prod ^:leaky {:main ~MAIN
                            :uberjar-name "app.standalone.jar"
                            :jar-name     "app.jar"
                            :uberjar-exclusions []
                            :aot  nil #_[datastore.serdes]}
             :uberjar {:aot :all
                       :native-image {:jvm-opts ["-Dclojure.compiler.direct-linking=true"]}}
             :hidpi-ui-scale {:jvm-opts ["-Dglass.gtk.uiScale=2"]}}

  :native-image {:name "app.native"            ;; name of output image, optional
                ;  :graal-bin "/path/to/graalvm/" ;; path to GraalVM home, optional
                 :opts ["--no-server" ;; pass-thru args to GraalVM native-image, optional
                        ; "--report-unsupported-elements-at-runtime"
                        "--allow-incomplete-classpath"
                        "--initialize-at-build-time"
                        "--enable-url-protocols=http"
                        "--verbose"
                        "--no-fallback"]}

  :main ^{:skip-aot false} ~MAIN
  :jvm-opts ["-Xms768m" "-Xmx11998m"]

  :source-paths ["src"]
  :java-source-paths ["src"]
  :test-paths [] #_["test"]
  :resource-paths [] #_["resources" "config"]
  :auto-clean false)