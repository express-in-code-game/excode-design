
## cljs self- hosting/compiling

- http://swannodette.github.io/2015/07/29/clojurescript-17/
- https://clojurescript.org/guides/self-hosting
- https://github.com/anmonteiro/lumo
    - https://anmonteiro.com/2017/02/compiling-clojurescript-projects-without-the-jvm/
- https://github.com/planck-repl/planck

## cljs

- cljs 1.10.749 Embracing JavaScript Tools
    - https://clojurescript.org/news/2020-04-24-bundle-target
    - https://clojurescript.org/guides/webpack
    - implications for sahdow-cljs
        - https://code.thheller.com/blog/shadow-cljs/2018/06/15/why-not-webpack.html
        - https://github.com/thheller/shadow-cljs/issues/706
            - 'Why not Webpack?' is still the answer, despite :bundle target
            - nevertheless, updated answer
                - https://code.thheller.com/blog/shadow-cljs/2020/05/08/how-about-webpack-now.html

## lein-cljsbuild

- https://github.com/emezeske/lein-cljsbuild
- https://github.com/emezeske/lein-cljsbuild/blob/1.1.8/sample.project.clj
- https://github.com/emezeske/lein-cljsbuild/blob/1.1.8/example-projects/advanced/project.clj


## figwheel-main

- https://figwheel.org/docs/editor-integration.html
- https://github.com/bhauman/figwheel-main


## vscode debug multiple extensions

- the argument --extensionDevelopmentPath can now be specified more than once.
    - https://github.com/microsoft/vscode/issues/72500
- https://code.visualstudio.com/docs/editor/debugging#_compound-launch-configurations
- https://code.visualstudio.com/docs/nodejs/nodejs-debugging#_launch-configuration-attributes


## vscode error : Proposed api on trying to log some vscode ref

- happens when (println {:context vscode-context-ref)

```
Error: [https://github.com/DeathStarGame/deathstar.ltee.deathstar.ltee]: Proposed API is only available when running out of dev or with the following command line switch: --enable-proposed-api https://github.com/DeathStarGame/deathstar.ltee.deathstar.ltee    at a (/usr/share/code/resources/app/out/vs/workbench/services/extensions/node/extensionHostProcess.js:605:324)    at Object.checkProposedApiEnabled (/usr/share/code/resources/app/out/vs/workbench/services/extensions/node/extensionHostProcess.js:605:686)    at Object.get logUri [as logUri] (/usr/share/code/resources/app/out/vs/workbench/services/extensions/node/extensionHostProcess.js:876:651)
```

- or same but for js/console.log 

```
[Extension Host] Unable to log remote console arguments Output omitted for an object that cannot be inspected ('Error: [https://github.com/DeathStarGame/deathstar.ltee.deathstar.ltee]: Proposed API is only available when running out of dev or with the following command line switch: --enable-proposed-api https://github.com/DeathStarGame/deathstar.ltee.deathstar.ltee')
```

- simple, but deadly - as it's unclear what causes it
- takeaway: don't log left and right vscode stuff


## nodejs worker_threads

- https://nodejs.org/api/worker_threads.html
- https://blog.soshace.com/advanced-node-js-a-hands-on-guide-to-event-loop-child-process-and-worker-threads-in-node-js/
- https://nodesource.com/blog/worker-threads-nodejs/
- https://blog.logrocket.com/node-js-multithreading-what-are-worker-threads-and-why-do-they-matter-48ab102f8b10/


## java sockets

- https://stackoverflow.com/questions/8406914/benefits-of-netty-over-basic-serversocket-server
- https://stackoverflow.com/questions/5385407/whats-the-difference-between-jetty-and-netty
- libs
    - https://github.com/TooTallNate/Java-WebSocket
    - https://github.com/socketio
        - https://github.com/socketio/engine.io-server-java 
        - https://github.com/socketio/socket.io-client-java


## nrepl middleware

- how default middleware is added
    - https://github.com/nrepl/nrepl/blob/master/src/clojure/nrepl/server.clj#L85
- how cider adds its middleware to nrepl's default
    - https://github.com/clojure-emacs/cider-nrepl/blob/master/src/cider/nrepl.clj#L527

## datomic

- https://www.reddit.com/r/Clojure/comments/8dqes8/datomic_look_at_all_the_things_im_not_doing/

## vscode extension samples

- https://github.com/Microsoft/vscode-extension-samples

## vscode multi-root workspace api

- https://github.com/microsoft/vscode/wiki/Adopting-Multi-Root-Workspace-APIs


## embedded databases

- https://en.wikipedia.org/wiki/Embedded_database

## electron and jvm

- graal exploring how they can integrate GraalVM with Electron 
    - https://github.com/oracle/graal/issues/326#issuecomment-475324066
- Java Chromium Embedded Framework (JCEF) 
    - https://github.com/chromiumembedded/java-cef
    - https://medium.com/@daniel.bischoff/integrating-chromium-as-a-web-renderer-in-a-java-application-with-jcef-72f67a677db6
- https://stackoverflow.com/questions/9235079/use-embedded-web-browser-e-g-chrome-as-gui-toolkit-for-java-desktop-applicati
- https://www.teamdev.com/jxbrowser
- https://www.java.com/en/download/faq/java_webstart.xml


## javafx tiny ui elements with 4k display resolution

- originally encountered here https://github.com/DeathStarGame/weekend-2020-07-23
- mistakenly thought it's ubuntu/jvm specific
- but encountered the same problem after installing davinci resolve 16
    - https://www.google.com/search?q=davinci+resolve+tiny+resolution+4k+display
    - the problem is encountered by many, including a lot of windows users
        - https://forum.blackmagicdesign.com/viewtopic.php?t=83845
        - https://forum.blackmagicdesign.com/viewtopic.php?t=95123
- so it's not jvm/ubutnu specific (since davinci gui is alegedly Qt based)

## understanding electron

- https://www.electronjs.org/docs
- https://www.electronjs.org/docs/api/browser-window
- https://www.electronjs.org/docs/api/browser-view
    - https://developer.chrome.com/apps/tags/webview
- creating native menu within web renderer
    - https://www.electronjs.org/docs/api/menu#render-process
- https://www.electronjs.org/docs/tutorial/application-architecture#main-and-renderer-processes
- https://github.com/microsoft/vscode/tree/master/src/vs/code/electron-main
- this is how WEbview in VSCode works (it's Electron's/Chromium's webview tag)
    - https://www.electronjs.org/docs/api/webview-tag


## java 14 jpackage

- https://openjdk.java.net/jeps/343
- it's called cross-compilation, and jpackage by design does not support it
```
There will be no support for cross compilation. 
For example, in order to create Windows packages one must run the tool on Windows. 
The packaging tool will depend upon platform-specific tools.
```
- electron's aprroach
    - https://www.electron.build/multi-platform-build

## auto updating java app

- https://github.com/edvin/fxlauncher

## controlfx devours CPU 

- https://github.com/controlsfx/controlsfx
- works with java11
- map contorl is definately done on canvas, in idle state uses 180% CPU like Starcraft at medium settings
- rediculously slow font icon grid, hover lags behind, CPU spins as if we are running a top notch graphical game
- it's just unacceptable

## javafx open source applications

- https://github.com/asciidocfx/AsciidocFX
- https://github.com/Col-E/Recaf
- https://github.com/torakiki/pdfsam
- https://dave.autonoma.ca/blog/2020/06/29/write-once-build-anywhere/
    - https://github.com/DaveJarvis/keenwrite
    - https://github.com/dgiagio/warp
    - https://github.com/JFormDesigner/markdown-writer-fx

## javafx vs electron

- https://twitter.com/tuxtor/status/1167454864932319232
- https://www.reddit.com/r/programming/comments/740bcd/say_no_to_electron_building_a_fast_responsive/
    - https://sites.google.com/a/athaydes.com/renato-athaydes/posts/saynotoelectronusingjavafxtowriteafastresponsivedesktopapplication
        - https://www.reddit.com/r/programming/comments/70jxpz/electron_the_bad_parts/
            - https://hackernoon.com/electron-the-bad-parts-2b710c491547
        - https://news.ycombinator.com/item?id=14245183
        - https://www.reddit.com/r/programming/comments/6br36z/native_code_is_still_the_best_alternative_to/
        - some other sdks
            - https://github.com/c-smile/sciter-sdk
            - https://github.com/nwjs/nw.js
- https://github.blog/2017-05-16-how-four-native-developers-wrote-an-electron-app/

## react native

- https://github.com/kusti8/proton-native
- https://microsoft.github.io/react-native-windows/
- https://github.com/status-im/react-native-desktop-qt
- https://github.com/flutter/flutter
    - https://flutter.dev/docs/resources/faq
    - https://flutter.dev/desktop
    - https://codelabs.developers.google.com/codelabs/flutter-github-graphql-client/index.html#0
- clojure/dart converstaion
    - https://groups.google.com/g/clojure/c/oJqhzurEajk
    - https://clojureverse.org/t/any-chance-to-run-clojure-on-dart/4240
- https://medium.com/@agent_hunt/introduction-to-react-native-renderers-aka-react-native-is-the-java-and-react-native-renderers-are-828a0022f433
    - https://github.com/yue/yue

## embeding chromium into jvm

- https://github.com/chromiumembedded/java-cef
    - https://en.wikipedia.org/wiki/Chromium_Embedded_Framework#Applications_using_CEF
- https://stackoverflow.com/questions/21192279/how-to-integrate-chromium-embedded-framework-cef-with-java

## hybrid approach: electron + jvm

- https://dzone.com/articles/desktop-uis-will-stay-alive-thanks-to-web-technolo
    - https://github.com/cuba-labs/java-electron-tutorial/blob/master/README.md
        - https://github.com/vaadin/