(ns starnet.pad.mat1
  (:require
   [clojure.repl :refer [doc]]
   [clojure.core.matrix :as mx]
   [clojure.core.matrix.operators :as mxo]
   [clojure.core.matrix.linear :as mxl]))


; https://github.com/mikera/core.matrix
; https://mikera.github.io/core.matrix/doc/index.html

(comment

  (mxl/norm (mx/sub [2 3] [3 3]) 2)

  (mx/distance [2 3] [3 3])

  (mxo/+ [[1 2]
          [3 4]]
         (mxo/* (mx/identity-matrix 2) 3.0))

  (mx/shape [[2 3 4] [5 6 7]])

  (mx/mmul
   (mx/array [[2 2] [3 3]])
   (mx/array [[4 4] [5 5]]))
  
  (mx/cos [90])
  #?(:clj (Math/cos 90))

  ;;
  )