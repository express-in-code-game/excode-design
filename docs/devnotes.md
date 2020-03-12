- kafka
  - https://kafka.apache.org/
  - https://kafka-tutorials.confluent.io/
  - https://kafka.apache.org/24/documentation/streams/developer-guide

  - https://kafka.apache.org/24/javadoc/org/apache/kafka/streams/kstream/KStream.html
  - https://kafka.apache.org/24/javadoc/org/apache/kafka/clients/consumer/KafkaConsumer.html
  - https://kafka.apache.org/24/javadoc/org/apache/kafka/clients/producer/KafkaProducer.html

  - https://github.com/perkss/clojure-kafka-examples
  - https://github.com/troy-west/kstream-examples

  - https://www.confluent.io/blog/event-sourcing-using-apache-kafka/
  - https://www.confluent.io/blog/building-a-microservices-ecosystem-with-kafka-streams-and-ksql/
  - https://github.com/confluentinc/kafka-streams-examples/tree/4.0.0-post/src/main/java/io/confluent/examples/streams/microservices
  - configs
    - http://kafka.apache.org/documentation.html#configuration
    - http://kafka.apache.org/documentation.html#topicconfigs

    - http://kafka.apache.org/documentation.html#producerconfigs
    - https://kafka.apache.org/24/javadoc/org/apache/kafka/clients/producer/KafkaProducer.html

    - http://kafka.apache.org/documentation.html#consumerconfigs
    - https://kafka.apache.org/24/javadoc/org/apache/kafka/clients/consumer/KafkaConsumer.html

- clj
  - spec
    - guides
      - https://clojure.org/guides/spec
      - https://clojure.github.io/spec.alpha/clojure.spec.alpha-api.html
      - https://clojure.org/guides/test_check_beginner
        - https://github.com/clojure/test.check/blob/master/README.md
        - https://github.com/clojure/test.check/blob/master/doc/intro.md
        - https://github.com/clojure/test.check/blob/master/doc/generator-examples.md
      - https://blog.taylorwood.io/2018/10/15/clojure-spec-faq.html
    - fdef defmulti
      - https://clojuredocs.org/clojure.spec.alpha/fdef#example-5c4b535ce4b0ca44402ef629
    - code
      - https://github.com/clojure/tools.deps.alpha/blob/master/src/main/clojure/clojure/tools/deps/alpha/specs.clj
  - java.time.Instant
    - https://stackoverflow.com/questions/36639154/convert-java-util-date-to-what-java-time-type
  - regex
    - https://docs.oracle.com/javase/9/docs/api/java/util/regex/Pattern.html
  - logic
    - http://programming-puzzler.blogspot.com/2013/03/logic-programming-is-overrated.html
      - https://gist.github.com/Engelberg/5105806#file-logic1-clj
      - https://gist.github.com/Engelberg/5105820#file-logic2-clj
      - http://swannodette.github.io/2013/03/09/logic-programming-is-underrated
      - https://gist.github.com/swannodette/5127144
      - https://gist.github.com/swannodette/5127150
    - https://github.com/clojure/core.logic
    - https://github.com/clojure/core.logic/wiki
    - https://github.com/clojure/core.logic/blob/master/src/test/clojure/clojure/core/logic
    - https://github.com/clojure/core.logic/tree/master/src/test/cljs/cljs/core/logic
    - https://github.com/clojure/core.logic/wiki/Features
    - https://github.com/clojure/core.logic/wiki/Examples
    - http://minikanren.org/
    - https://blog.taylorwood.io/2018/05/10/clojure-logic.html
    - https://jrheard.tumblr.com/post/43575891007/explorations-in-clojures-corelogic
      - https://www.infoq.com/presentations/core-logic/#mainLogin/
    - http://swannodette.github.io/
    - https://web.archive.org/web/20130511050744/http://dosync.posterous.com/a-logic-programming-reading-list
    - http://tgk.github.io/2012/08/finding-cliques-in-graphs-using-core-logic.html
    - https://web.archive.org/web/20130511050720/http://dosync.posterous.com/know-your-bounds
    - http://clojurelx.blogspot.com/2012/01/finite-state-machines-in-corelogic.html
    - http://clojurelx.blogspot.com/2012/01/lx-in-corelogic-2-jumps-flexible.html
    - http://gigasquidsoftware.com/chemical-computing/index.html
  - state abstractions
    - https://github.com/day8/re-frame
    - https://github.com/tonsky/datascript
      - https://tonsky.me/blog/datascript-chat/
    - https://github.com/juxt/crux
    - https://github.com/clojure/core.async
      - https://www.infoq.com/presentations/clojure-core-async/
        - "good programs should be made out of processes and queues"

- cljs
  - antd
    - 4.0 changes https://github.com/ant-design/ant-design/issues/16911


- issues
  - "attempting to call unbound transit-json..." when ineracting with kafka from repl
    - starnet.app.alpha.aux.serdes namespace must be imported 