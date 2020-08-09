- terminology
  - volunteer cloud
    - https://en.wikipedia.org/wiki/Cloud_computing#Community_cloud
  - distributed computing
    - https://en.wikipedia.org/wiki/Distributed_computing
  - decentralized application
    - https://en.wikipedia.org/wiki/Decentralized_application


- kubernetes
  - https://kubernetes.io/docs/concepts/
    - https://kubernetes.io/docs/concepts/overview/what-is-kubernetes/
  - https://kubernetes.io/docs/concepts/architecture/nodes/#management
    - "The kubelet on a node self-registers to the control plane"
  - https://kubernetes.io/docs/concepts/architecture/control-plane-node-communication/#node-to-control-plane
    - " the default operating mode for connections from the nodes and pods running on the nodes to the control plane is secured by default and can run over untrusted and/or public networks"
  - https://kubernetes.io/blog/2016/10/globally-distributed-services-kubernetes-cluster-federation/
  - kind (to run k8s in docker)
    - https://github.com/kubernetes-sigs/kind
    - https://kind.sigs.k8s.io/docs/user/quick-start/
    - alternative
      - https://github.com/bsycorp/kind
  - installing production k8s
    - https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/high-availability/
  - self-hosted
    - https://github.com/kubernetes-sigs/bootkube
    - https://github.com/kubernetes/community/blob/master/contributors/design-proposals/cluster-lifecycle/self-hosted-kubernetes.md
  - microK8s
    - https://github.com/ubuntu/microk8s
    - https://microk8s.io/docs/clustering

  - examples
    - https://github.com/kubernetes/examples
  - gitkube
    - https://github.com/hasura/gitkube
  - agones
    - https://github.com/googleforgames/agones
    - https://agones.dev/site/docs/overview/
  - reconfiguring kubelet dynamically
    - https://kubernetes.io/docs/tasks/administer-cluster/reconfigure-kubelet/#reconfiguring-the-kubelet-on-a-running-node-in-your-cluster

- swarm
  - https://docs.docker.com/engine/swarm/
  - https://docs.docker.com/engine/swarm/stack-deploy/
  - docker in docker
    - https://callistaenterprise.se/blogg/teknik/2017/12/18/docker-in-swarm-mode-on-docker-in-docker/
  - https://docs.docker.com/engine/swarm/how-swarm-mode-works/nodes/  
  - routing mesh
    - https://docs.docker.com/engine/swarm/ingress/
  - plugins 
    - https://docs.docker.com/engine/extend/
    - https://docs.docker.com/engine/extend/plugins_services/

- cloud standards
  - https://en.wikipedia.org/wiki/Open_Container_Initiative
    - https://opencontainers.org/

- containerd
  - https://www.docker.com/blog/what-is-containerd-runtime/

- autoscaling
  - k8s
    - https://kubernetes.io/blog/2016/07/autoscaling-in-kubernetes/
    - https://github.com/kubernetes/autoscaler/tree/master/cluster-autoscaler
  - swarm
    - https://stackoverflow.com/questions/41668621/how-to-configure-autoscaling-on-docker-swarm
    - https://forums.docker.com/t/autoscaling-in-docker-swarm/44353/2

- service mesh
  - https://github.com/linkerd/linkerd
  - https://github.com/linkerd/linkerd2
    - https://servicemesh.io/
    - https://linkerd.io/2/overview/
  - https://github.com/istio/istio

- docker engine api
  - https://docs.docker.com/engine/api/
  - https://docs.docker.com/engine/api/v1.40/
  - https://docs.docker.com/engine/api/sdk/examples/

- nats
  - https://siliconangle.com/2020/01/13/nats-messaging-could-beat-kubernetes-to-the-edge-kubecon-startupoftheweek/
  - https://github.com/nats-io
  - https://docs.nats.io/whats_new_20

- edge computing
  - https://en.wikipedia.org/wiki/Edge_computing
  - https://github.com/kubeedge/kubeedge

- docker in docker
  - https://hub.docker.com/_/docker
    - https://jpetazzo.github.io/2015/09/03/do-not-use-docker-in-docker-for-ci/
    - rootles
      - https://docs.docker.com/engine/security/rootless/
    - where to store data 
      - data can be persisted using -v /my/own/var-lib-docker:/var/lib/docker

- snaps
  - https://snapcraft.io/

- k3s
  - https://rancher.com/press/2019-02-26-press-release-rancher-labs-introduces-lightweight-distribution-kubernetes-simplify/
  - https://github.com/rancher/k3s
  - https://rancher.com/docs/k3s/latest/en/architecture/
  - k3s in docker
    - https://rancher.com/docs/k3s/latest/en/advanced/#running-k3d-k3s-in-docker-and-docker-compose
    - https://github.com/rancher/k3d
    - https://github.com/rancher/k3s/blob/master/docker-compose.yml
  - concerns
    - no ha-cluster with stacked datalayer (only external?)
      - https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/ha-topology/
      - https://rancher.com/docs/k3s/latest/en/architecture/#high-availability-k3s-server-with-an-external-db
      - experimental support for embedded
        - https://rancher.com/docs/k3s/latest/en/installation/ha-embedded/
        - https://github.com/canonical/dqlite
