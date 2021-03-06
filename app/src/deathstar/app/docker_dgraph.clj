(ns deathstar.app.docker-dgraph
  (:gen-class)
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.string]
   [clojure.spec.alpha :as s]
   [clojure.java.io]

   [clj-docker-client.core :as docker]))

(defonce ^:private registry-ref (atom {}))

(defn create-opts
  [{:keys [::id] :or {id :main} :as opts}]
  (let [suffix (str "-" (name id))]
    (merge
     {::image-name "dgraph/dgraph:v20.11.2"
      ::volume-name (str "deathstar-dgraph" suffix)
      ::network-name (str "deathstar-network" suffix)
      ::zero-name (str "deathstar-dgraph-zero" suffix)
      ::alpha-name (str "deathstar-dgraph-alpha" suffix)
      ::ratel-name (str "deathstar-dgraph-ratel" suffix)
      ::alpha-port 3088
      ::ratel-port 8000
      ::remove-volume? false}
     opts)))

(def dev-preset (create-opts
                 {::id :main}))

(comment

  (up dev-preset)
  (down dev-preset)
  
  ;;
  )


(def docker-api-version "v1.41")
      
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

(defn create-image
  [opts]
  (go
    (let []
      (docker/invoke images
                     {:op     :ImageCreate
                      :params {:fromImage (::image-name opts)}}))))

(defn create-volume
  [opts]
  (go
    (let []
      (docker/invoke volumes
                     {:op     :VolumeCreate
                      :params {:body {:Name (::volume-name opts)}}}))))

(defn remove-volume
  [opts]
  (go
    (let []
      (docker/invoke volumes
                     {:op     :VolumeRemove
                      :params {:name (::volume-name opts)}}))))

(defn create-network
  [opts]
  (go
    (let []
      ;; does not work {:message "got EOF while reading request body"}
      (docker/invoke networks
                     {:op     :NetworkCreate
                      :params {:body {:Name (::network-name opts)
                                      :Internal false}}}))))

(defn remove-network
  [opts]
  (go
    (let []
      (docker/invoke networks
                     {:op     :NetworkRemove
                      :params {:id (::network-name opts)}}))))

(defn create-containers
  [opts]
  (go
    (let []
      (docker/invoke containers
                     {:op     :ContainerCreate
                      :params {:name (::zero-name opts)
                               :body {:Image (::image-name opts)
                                      :Cmd   ["dgraph"  "zero" "--my=zero:5080"]
                                      ;; :ExposedPorts {"5080/tcp" {}}
                                      :HostConfig {:Binds [(format "%s:/dgraph" (::volume-name opts))]}
                                      :NetworkingConfig  {:EndpointsConfig
                                                          {(::network-name opts)
                                                           {"Aliases" ["zero"]}}}}}})
      (docker/invoke containers
                     {:op     :ContainerCreate
                      :params {:name (::alpha-name opts)
                               :body {:Image (::image-name opts)
                                      :Cmd   ["dgraph" "alpha"
                                              "--my=alpha:7080" "--zero=zero:5080"
                                              "--whitelist" "0.0.0.0/0"]
                                      ;; :ExposedPorts {"8080/tcp" {}
                                      ;;                "9080/tcp" {}}
                                      :HostConfig {:Binds
                                                   [(format "%s:/dgraph" (::volume-name opts))]
                                                   :PortBindings
                                                   {"8080/tcp"
                                                    [{"HostPort" (str (::alpha-port opts) ) }]}}
                                      :NetworkingConfig  {:EndpointsConfig
                                                          {(::network-name opts)
                                                           {"Aliases" ["alpha"]}}}}}})
      (docker/invoke containers
                     {:op     :ContainerCreate
                      :params {:name (::ratel-name opts)
                               :body {:Image (::image-name opts)
                                      :Cmd    ["dgraph-ratel"]
                                      ;; :ExposedPorts {"8000/tcp" {}}
                                      :HostConfig {:Binds
                                                   [(format "%s:/dgraph" (::volume-name opts))]
                                                   :PortBindings
                                                   {"8000/tcp"
                                                    [{"HostPort" (str (::ratel-port opts))}]}}
                                      :NetworkingConfig  {:EndpointsConfig
                                                          {(::network-name opts)
                                                           {"Aliases" ["ratel"]}}}}}}))))

(defn start-containers
  [opts]
  (go
    (let []
      (docker/invoke containers {:op     :ContainerStart
                                 :params {:id (::zero-name opts)}})
      (docker/invoke containers {:op     :ContainerStart
                                 :params {:id (::alpha-name opts)}})
      (docker/invoke containers {:op     :ContainerStart
                                 :params {:id (::ratel-name opts)}}))))

(defn stop-containers
  [opts]
  (go
    (let []
      (docker/invoke containers
                     {:op     :ContainerStop
                      :params {:id (::zero-name opts)}})
      (docker/invoke containers
                     {:op     :ContainerStop
                      :params {:id (::alpha-name opts)}})
      (docker/invoke containers
                     {:op     :ContainerStop
                      :params {:id (::ratel-name opts)}}))))

(defn remove-containers
  [opts]
  (go
    (let []
      (docker/invoke containers
                     {:op     :ContainerDelete
                      :params {:id (::zero-name opts)
                               :v true}})
      (docker/invoke containers
                     {:op     :ContainerDelete
                      :params {:id (::alpha-name opts)
                               :v true}})
      (docker/invoke containers
                     {:op     :ContainerDelete
                      :params {:id (::ratel-name opts)
                               :v true}}))))

(defn up
  [opts]
  (go
    (let []
      (<! (create-image opts))
      (println ::pulled-image)
      (<! (create-volume opts))
      (println ::created-volume)
      (<! (create-network opts))
      (println ::created-network)
      (<! (create-containers opts))
      (println ::created-containers)
      (<! (start-containers opts))
      (println ::started-containers))))

(defn down
  [{:keys [::remove-volume?] :as opts}]
  (go
    (<! (stop-containers opts))
    (println ::stoped-containers)
    (<! (remove-containers opts))
    (println ::removed-containers)
    #_(<! (remove-network opts))
    #_(println ::removed-network)
    (when remove-volume?
      (<! (remove-volume opts))
      (println ::removed-volume))))


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
            (some #(clojure.string/includes? % "app") (:RepoTags img))) image-list)

 ;;
  )

(defn count-images
  []
  (go (let [image-list (docker/invoke images {:op :ImageList})]
        (println ::docker-images (count image-list)))))