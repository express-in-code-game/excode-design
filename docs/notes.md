
- build a vscode extension
- use deathstar and cljctools namespaces

## experiments

- go w/o shadow-cljs? possible ? w/ cljs build only ?

## (:require-macros [])

- https://cljs.github.io/api/cljs.core/defmacro
- https://clojurescript.org/guides/ns-forms
- https://code.thheller.com/blog/shadow-cljs/2019/10/12/clojurescript-macros.html

- the way it's done with deathstar.spec
  - deathstar.spec.cljc is one file that is both macro ns and runtime ns
  - it requires itself using reader conditional  #?(:cljs (:require-macros [deathstar.spec]))
  - why
    - because we need this whole ns at compile time (clojure time) and runtime (cljs)
  - how it works
    - simple: first file is read as .cljc for cljs
    - conditional returns (:require-macros) form, which tell compiler to look for macros
    - since ns is the same, it reads the same file again, but this time as macro ns (clj), reader conditional is skipped (so it does not error because :require-macros is cljs only)
  - what happens
    - when file is read as macros, it's evaled in clj during compilation and spec can be used during macroexpansion
    - when it's read as cljs file, it becomes part of runtime