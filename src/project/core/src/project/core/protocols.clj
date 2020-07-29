(ns project.core.protocols)

(defprotocol Mountable
  :extend-via-metadata true
  (mount* [_])
  (unmount* [_]))