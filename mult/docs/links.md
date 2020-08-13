
#### core.async

- https://github.com/clojure/core.async/blob/master/src/main/clojure/clojure/core/async.clj#L881

#### vscode

- api 
  - https://code.visualstudio.com/api/references/vscode-api#TextEditor
- https://github.com/microsoft/vscode-extension-samples/tree/master/helloworld-sample
- api
  - https://code.visualstudio.com/api/extension-guides/command#registering-a-command
- launch.json
  - https://code.visualstudio.com/api/working-with-extensions/testing-extension#debugging-the-tests
- sample extentions in cljs
  - https://github.com/Saikyun/cljs-vscode-extension-hello-world
- sample websocket
  - https://github.com/microsoft/vscode-extension-samples/blob/master/lsp-log-streaming-sample/client/src/extension.ts
- webview
  - https://code.visualstudio.com/api/extension-guides/webview

#### shadow-cljs

- https://shadow-cljs.github.io/docs/UsersGuide.html#target-node-library
- https://shadow-cljs.github.io/docs/UsersGuide.html#_calva_vs_code
- https://shadow-cljs.github.io/docs/UsersGuide.html#cljs-repl-anatomy
- https://shadow-cljs.github.io/docs/UsersGuide.html#missing-js-runtime
- required js libs in node won't be processed
  - https://shadow-cljs.github.io/docs/UsersGuide.html#js-provider
- "Error in phase :compile-syntax-check: No such var: ioc/state-machine" in shadow-client-1.3.2.jar!/shadow/util.cljs
- resolved: https://github.com/sergeiudris/shadow/commit/320ea4abd28b18d7a576183a8df37446f9716e52
- related
  - https://github.com/mfikes/andare/issues/2
    - https://github.com/mfikes/andare/commit/9419fa02f49c293f90da80bb62134e5f2aed94ea#diff-283073ca0e5bfffccdd5ce09cbcc002aL18

#### cljs repl

- https://clojurescript.org/reference/repl
- cljs repls evaluations envs 
  - https://github.com/clojure/clojurescript/tree/master/src/main/clojure/cljs/repl
  - browser 
    - https://github.com/clojure/clojurescript/blob/master/src/main/clojure/cljs/repl/browser.clj#L389
  - node 
    - https://github.com/clojure/clojurescript/blob/master/src/main/clojure/cljs/repl/node.clj#L206
  - repl eval
    - https://github.com/clojure/clojurescript/blob/master/src/main/clojure/cljs/repl.cljc#L1162
- browser connect & xpc-connection 
  - https://github.com/clojure/clojurescript/blob/master/src/main/cljs/clojure/browser/repl.cljs#L226
  - https://github.com/clojure/clojurescript/blob/master/src/main/cljs/clojure/browser/net.cljs#L117

#### nrepl

- https://github.com/nrepl/nrepl
  - https://nrepl.org/nrepl/ops.html
  - https://nrepl.org/nrepl/0.7.0/usage/clients.html#_talking_to_an_nrepl_endpoint_programmatically
  - https://github.com/nrepl/nrepl/blob/master/src/spec/nrepl/spec.clj
- https://github.com/nrepl/piggieback
- https://github.com/nrepl/weasel
- examples of connecting from not-java
  - https://nrepl.org/nrepl/0.7.0/usage/clients.html
    - https://github.com/BetterThanTomorrow/calva/tree/master/src/nrepl
    - https://github.com/rksm/node-nrepl-client/blob/master/src/nrepl-client.js
- https://docs.cider.mx/cider-nrepl/internals.html#_clojurescript_support
- api
  - nrepl.server/start-server
    - https://github.com/nrepl/nrepl/blob/master/src/clojure/nrepl/server.clj#L109
  - nrepl.core/message
    - https://github.com/nrepl/nrepl/blob/master/src/clojure/nrepl/core.clj#L80
  - nrepl.transport/bencode (is a transport-fn)
    - https://github.com/nrepl/nrepl/blob/master/src/clojure/nrepl/transport.clj#L93
  - sessions
    - https://nrepl.org/nrepl/design/middleware.html#_sessions

#### nodejs

- https://nodejs.org/api/net.html#net_class_net_socket
- https://nodejs.org/api/net.html#net_socket_connect_options_connectlistener
- https://nodejs.org/api/net.html#net_net_createconnection_options_connectlistener

#### paredit

- https://www.emacswiki.org/emacs/ParEdit
- http://mumble.net/~campbell/emacs/paredit.html
- https://github.com/MarcoPolo/atom-paredit/blob/master/cljs/paredit/core.cljs
- https://github.com/BetterThanTomorrow/calva/tree/master/src/paredit
- https://github.com/clj-commons/rewrite-cljs/blob/master/src/rewrite_clj/paredit.cljs
- https://github.com/laurentpetit/paredit.clj
  - https://github.com/ccw-ide/ccw/tree/master/paredit.clj
- https://github.com/rksm/paredit.js

#### github

- linguist
  - https://github.com/github/linguist/issues/137
  - https://github.com/github/linguist#overrides

#### protocols

- https://www.infoq.com/interviews/hickey-clojure-protocols/
  - "...it's not like you're putting it on string; you're saying there is a fn whose behavior I'd like to be different when it's passed strings... "
- https://github.com/clojure/clojurescript/blob/c057c92bd20bb4bea61970fb87247582ae2f5423/src/main/cljs/cljs/core.cljs
- https://github.com/clojure/spec.alpha/blob/master/src/main/clojure/clojure/spec/alpha.clj
- https://github.com/clojure/core.async/blob/1b8b972372e570d47959887c00a8726472236e74/src/main/clojure/clojure/core/async/impl/protocols.clj


#### cljsjs

- https://clojurescript.org/guides/self-hosting
- https://github.com/mfikes/ambient
- https://gist.github.com/mfikes/66a120e18b75b6f4a3ecd0db8a976d84
- https://code.thheller.com/blog/shadow-cljs/2017/10/14/bootstrap-support.html
- eval not working
  - eval works with default clojuscript compilation for both browser and node
  - if extension is compiled with 'bash f cljs_compile' 
    - and run with node *or with vscode*, eval works
      - but only if shadow-cljs is not on classpath
      - otherwise *target* becomes unset and exception thrown 'find-ns-obj not supported for target'
    - on vscode extension: simply adding 
      ```clojure
        (when (exists? js/module)
        (set! js/module.exports #js {:activate (fn [] ) :deactivate (fn [] )} )
      ```
      to cljc node comilation with optimization simple makes it a vscode extension 
  - when compiled with shadow-cljs, eval of fn, defn, let etc. fails with 'SyntaxError: Unexpected token '.''
  - points:
    - fundamentally, everything works as expected, with core.async and everything
    - and runs in vscode
    - problem is somehow linked to shadow-cljs
  - clojuresciprt compilation and shadow-cljs 
    - conflict of versions of these deps
      - com.google.javascript/closure-compiler-unshaded
      - org.clojure/google-closure-library
    - if shadow is on the calsspath, it's dependencies cause cljs cli comilation to fail
    - SyntaxError: Unexpected token .   stackpath
    - https://github.com/clojure/clojurescript/blob/master/src/main/cljs/cljs/js.cljs#L121
    - https://github.com/clojure/clojurescript/blob/master/src/main/cljs/cljs/js.cljs#L841
    - https://github.com/clojure/clojurescript/blob/master/src/main/cljs/cljs/js.cljs#L844
    - https://github.com/clojure/clojurescript/blob/master/src/main/cljs/cljs/js.cljs#L1244
    - after modifying and using local clojurescript (to print the values at the point of error),it's clear:
      - error is caused by missing of /let /x /fn etc.
      - possible answer -  lack of analyzer data
        - https://clojureverse.org/t/different-behaviour-between-lein-cljsbuild-and-shadow-cljs/4497
        - https://code.thheller.com/blog/shadow-cljs/2017/10/14/bootstrap-support.html
        - https://github.com/thheller/shadow-cljs/blob/master/src/main/shadow/cljs/bootstrap/node.cljs
      - but core.async is not shadow-boostrap compatible
        - https://clojurians-log.clojureverse.org/shadow-cljs/2018-04-08/1523194511.000050
      - self-host compatible fork of core.async
        - https://github.com/mfikes/andare
    - solved
      - andare as core.async with shadow-cljs :bootstrap for node work

        
#### clojuresciprt

- to use as {:local/root "../../clojurescript"}  do git checkout r1.10.597


#### figwheel-main

- nrepl
  - https://github.com/bhauman/figwheel-main/issues/112
  - https://github.com/bhauman/figwheel-main/issues/24

#### git

- https://stackoverflow.com/questions/2973996/git-rebase-without-changing-commit-timestamps
- https://stackoverflow.com/questions/2427238/in-git-what-is-the-difference-between-merge-squash-and-rebase
- https://stackoverflow.com/questions/5308816/how-to-use-git-merge-squash
- https://stackoverflow.com/questions/8939977/git-push-rejected-after-feature-branch-rebase
  - git push --force-with-lease
    - https://stackoverflow.com/questions/52823692/git-push-force-with-lease-vs-force
- https://stackoverflow.com/questions/9069061/what-is-the-difference-between-git-merge-and-git-merge-no-ff
- https://stackoverflow.com/questions/173919/is-there-a-theirs-version-of-git-merge-s-ours
- git push <remote> src-branch:<remote-dst-branch>
- git merge --no-ff --squash remote/branch
- https://stackoverflow.com/questions/9110310/update-git-commit-author-date-when-amending/13497026
  - git commit --amend --no-edit --date=now 

#### clojure editor experience

- options
  - Emacs Cider
  - IntelliJ Cursive
  - VSCode Calva
  - Neovim Conjure
    - https://github.com/Olical/conjure
- misc
  - https://www.reddit.com/r/Clojure/comments/8vsirv/is_counterclockwise_on_eclipse_dead/

#### open source jvm editors

- https://github.com/JetBrains/intellij-community


#### Socket REPL

- clojure group discussion in relation to vscode
  - https://groups.google.com/g/clojure/c/Na7npowHssY/m/t7kDBfCGCgAJ
    - https://github.com/mauricioszabo/atom-chlorine
    - https://github.com/mauricioszabo/clover
- https://oli.me.uk/exploring-repl-tooling-with-prepl/