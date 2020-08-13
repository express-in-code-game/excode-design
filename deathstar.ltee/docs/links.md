
#### cljs self- hosting/compiling

- http://swannodette.github.io/2015/07/29/clojurescript-17/
- https://clojurescript.org/guides/self-hosting
- https://github.com/anmonteiro/lumo
    - https://anmonteiro.com/2017/02/compiling-clojurescript-projects-without-the-jvm/
- https://github.com/planck-repl/planck

#### cljs

- cljs 1.10.749 Embracing JavaScript Tools
    - https://clojurescript.org/news/2020-04-24-bundle-target
    - https://clojurescript.org/guides/webpack
    - implications for sahdow-cljs
        - https://code.thheller.com/blog/shadow-cljs/2018/06/15/why-not-webpack.html
        - https://github.com/thheller/shadow-cljs/issues/706
            - 'Why not Webpack?' is still the answer, despite :bundle target
            - nevertheless, updated answer
                - https://code.thheller.com/blog/shadow-cljs/2020/05/08/how-about-webpack-now.html

#### lein-cljsbuild

- https://github.com/emezeske/lein-cljsbuild
- https://github.com/emezeske/lein-cljsbuild/blob/1.1.8/sample.project.clj
- https://github.com/emezeske/lein-cljsbuild/blob/1.1.8/example-projects/advanced/project.clj


#### figwheel-main

- https://figwheel.org/docs/editor-integration.html
- https://github.com/bhauman/figwheel-main


#### vscode debug multiple extensions

- the argument --extensionDevelopmentPath can now be specified more than once.
    - https://github.com/microsoft/vscode/issues/72500
- https://code.visualstudio.com/docs/editor/debugging#_compound-launch-configurations
- https://code.visualstudio.com/docs/nodejs/nodejs-debugging#_launch-configuration-attributes