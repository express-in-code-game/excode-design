
# origin: a volunteer automated cluster

## rationale

- make a kubernetes/swarm alternative, simpler, and designed differently

## design notes

- existing solutions 
  - take an approach of control and predefined role nodes
    - swarm allows to change roles, but is too low level: building around swarm seems more complicated than starting anew
  - do not (except for k3s) run nodes in containers by default: nodes are tightly coupled to machines
  - rely on CLI to "control" the cluster, as opposed to automated and version controlled config/spec, zero CLI
  - see cluster as a mechanism to control, rather than an automated self-forming system, behavior of which is predefined during the node image build
  - do not support by default a system that can automatically grow from one node to many and shrink back to one node without any CLI inputs, completely predefined by config
  - only swarm and k3s are by default tls-secure

- system using the cluster
  - system should be able to build a system node image around cluster node
  - system makes the image available to download
  - wherever an image is run, it auto joins the cluster or originates one
  - so system's cluster is formed automatically, with behavior predefined by files encapsulated inside the image

- discovery
  - machines join the cloud dynamically
  - discovery starts with a list: public domains (e.g. github repos) are used in lookupd/dns like manner

- cluster growth/shrinkage
  - cluster can originate, grow from one to many nodes and shrink back to one node
  - when node image starts, it either joins the cluster or originates a clsuter
  - when a new node join the cluster, current hub nodes check if new node has better characteristics to be a hub node
  - if conditions are met, one of the hub nodes is demoted and new noted is promoted

- what cluster should be
  - node represents address(id) and resources, there should be only one type of node: node
  - node can add/remove additional responsility/role by starting/stoppping additional processes
  - there should not be any commands on the node machines except for starting the origin image (node)
  - everything else is within files that describe how cluster works
  - node image should have it's own config to join the cluster, but again, as data (file), no cli
  - after nodes join, cluster automatically assigns roles and distributes responsibiltiy between nodes according to files
  - if node should become a key node, it dynamically downloads (if needed) additional images and/or data and starts processes
  - on demotion, it stops processes and removes data
  - cluster always present
    - say, cluster config specifies e.g. "system needs at least 20 machines"
    - when origin image is started on a machine, cluster begins to exist
    - however, cluster acts according to config : no processes are started and no ops are performed
    - after machine count reaches 20 , cluster starts the processes and handles requests
    - if machines go below the min number, cluster stops procs or pauses (again, specified in config), waits for machines to join

- cluster installation
  - prerequisite for node installation is docker/k8s
  - this way a node may host multiple systems and installation is OS-agnostic
  - node should be installed as a container

- cluster spec/config
  - clsuter spec/config should be in files, version controled
  - can be config or code (plugins or scripts)

- cluster interface
  - no ui besides an editor
  - use git repo of clojure files to interact/operate on the cluster using REPL
  - e.g. eval expression to donwload log files, open them in editor, or requests cluster state or stats

- controlling the cluster
  - certain (e.g. cluster control) operations require security keys
  - keys for the clsuter TLS are stored in a private repo and used during image builds
  - once origin image is built, cluster can be stared (originate) anywhere (at any node) using the origin image

- node image
  - node runs in docker on the same level as apps
  - node exposes api (http/tcp/que) that apps use, and proxies to apps
  - node proc starts/stops apps etc. as it is --privileged in docker
  - this way node has tls and other layers that apps should not be aware off
  - so node is 
    - a layer between app containers and the rest of the cluster
    - a clsuter interface for apps
  - node stars app containers and only accepts requests from these containers

- cluster abstraction
  - cluster is built as a composition of layers (processes)
  - should be possible to replace a lavel (process) with an alternative implementation
  - should be possible to compile into a single binary or multiple
  - external tools (e.g. ingress or db) should run as containers alongside
  - consider using code (fns in .edn config and/or clojure code)
    - should be possible to add files and compile a customized cluster
    - code may e.g. access other procs apis (via channels)
    - or simply replace an implementation


- building images
  - cluster node can pull source grom git and build needed system images
  - the system may have e.g. a dir that conatains cluster logic or it may be a separate repo
  - so usage of registry is optional: only node image of the cluster must be pull onto the machine, else can be built from code