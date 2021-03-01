(ns deathstar.app.docker
  (:gen-class)
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.string :as str]
   [clojure.spec.alpha :as s]
   [clojure.java.io :as io]

   [clj-docker-client.core :as docker]))





(def ^:const docker-api-version "v1.41")
      
(def containers (docker/client {:category    :containers
                                :conn        {:uri "unix:///var/run/docker.sock"}
                                :api-version docker-api-version}))

(def images (docker/client {:category    :images
                            :conn        {:uri "unix:///var/run/docker.sock"}
                            :api-version docker-api-version}))

(def volumes (docker/client {:category    :volumes
                             :conn        {:uri "unix:///var/run/docker.sock"}
                             :api-version docker-api-version}))

(def networks (docker/client {:category    :networks
                              :conn        {:uri "unix:///var/run/docker.sock"}
                              :api-version docker-api-version}))

(comment

  (docker/categories docker-api-version)

  (def images (docker/client {:category :images
                              :api-version docker-api-version
                              :conn     {:uri "unix:///var/run/docker.sock"}}))

  (docker/ops images)

  (def image-list (docker/invoke images {:op     :ImageList}))
  (count image-list)

  (->> image-list
       (drop 5)
       (take 5))

  (filter (fn [img]
            (some #(str/includes? % "app") (:RepoTags img))) image-list)

 ;;
  )

(defn count-images
  []
  (go (let [image-list (docker/invoke images {:op :ImageList})]
        (println ::docker-images (count image-list)))))

(defn start-dgraph
  []
  (go
    (let [dgraph-image-name "dgraph/dgraph:v20.11.2"
          dgraph-volume-name "deathstar-dgraph"
          dgraph-network-name "deathstar-network"]
      (docker/invoke volumes {:op     :VolumeCreate
                              :params {:body {:Name dgraph-volume-name}}})
      #_(docker/invoke volumes {:op     :VolumeRemove
                                :params {:name dgraph-volume-name}})
      (println ::dgraph ::created-volume)

      ;; does not work {:message "got EOF while reading request body"}
      #_(docker/invoke networks {:op     :NetworkCreate
                               :params {:body {:Name "deathstar-network"
                                               :Internal false}}})
      #_(println ::dgraph ::created-network)
      #_(docker/invoke networks {:op     :NetworkRemove
                                 :params {:id "deathstar-network"}})
      (docker/invoke images {:op     :ImageCreate
                             :params {:fromImage dgraph-image-name}})

      (docker/invoke containers {:op     :ContainerCreate
                                 :params {:name "deathstar-dgraph-zero"
                                          :body {:Image dgraph-image-name
                                                 :Cmd   ["dgraph"  "zero" "--my=zero:5080"]
                                                 :ExposedPorts {"5080/tcp" {}}
                                                 :HostConfig {:Binds [(format "%s:/dgraph" dgraph-volume-name)]}
                                                 :NetworkingConfig  {:EndpointsConfig
                                                                     {dgraph-network-name
                                                                      {"Aliases" ["zero"]}}}}}})
      (docker/invoke containers {:op     :ContainerCreate
                                 :params {:name "deathstar-dgraph-alpha"
                                          :body {:Image dgraph-image-name
                                                 :Cmd   ["dgraph" "alpha" "--my=alpha:7080" "--zero=zero:5080"]
                                                 :ExposedPorts {"8080/tcp" {}
                                                                "9080/tcp" {}}
                                                 :HostConfig {:Binds
                                                              [(format "%s:/dgraph" dgraph-volume-name)]
                                                              :PortBindings
                                                              {"8080/tcp"
                                                               [{"HostPort" "8080"}]}}
                                                 :NetworkingConfig  {:EndpointsConfig
                                                                     {dgraph-network-name
                                                                      {"Aliases" ["alpha"]}}}}}})
      (docker/invoke containers {:op     :ContainerCreate
                                 :params {:name "deathstar-dgraph-ratel"
                                          :body {:Image dgraph-image-name
                                                 :Cmd    ["dgraph-ratel"]
                                                 :ExposedPorts {"8000/tcp" {}}
                                                 :HostConfig {:Binds
                                                              [(format "%s:/dgraph" dgraph-volume-name)]
                                                              :PortBindings
                                                              {"8000/tcp"
                                                               [{"HostPort" "8000"}]}}
                                                 :NetworkingConfig  {:EndpointsConfig
                                                                     {dgraph-network-name
                                                                      {"Aliases" ["ratel"]}}}}}})
      (println ::dgraph ::created-containers)
      (docker/invoke containers {:op     :ContainerStart
                                 :params {:id "deathstar-dgraph-zero"}})
      (docker/invoke containers {:op     :ContainerStart
                                 :params {:id "deathstar-dgraph-alpha"}})
      (docker/invoke containers {:op     :ContainerStart
                                 :params {:id "deathstar-dgraph-ratel"}})
      (println ::dgraph ::started-containers))))

(defn stop-dgraph
  []
  (go
    (let []
      (docker/invoke containers {:op     :ContainerStop
                                 :params {:id "deathstar-dgraph-zero"}})
      (docker/invoke containers {:op     :ContainerDelete
                                 :params {:id "deathstar-dgraph-zero"
                                          :v true}})
      (docker/invoke containers {:op     :ContainerStop
                                 :params {:id "deathstar-dgraph-alpha"}})
      (docker/invoke containers {:op     :ContainerDelete
                                 :params {:id "deathstar-dgraph-alpha"
                                          :v true}})
      (docker/invoke containers {:op     :ContainerStop
                                 :params {:id "deathstar-dgraph-ratel"}})
      (docker/invoke containers {:op     :ContainerDelete
                                 :params {:id "deathstar-dgraph-ratel"
                                          :v true}}))))