(ns project.core.protocols)

(defprotocol Mountable
  :extend-via-metadata true
  (mount* [_] [_ opts])
  (unmount* [_] [_ opts]))