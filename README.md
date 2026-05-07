# C-Balancer: Intelligent Container Placement and Scheduling Framework

## Overview

C-Balancer is a scheduling framework designed for efficient placement and dynamic rebalancing of containers in clustered environments. It addresses critical challenges in container orchestration by handling resource contention, providing container migration capabilities, and optimizing cluster resource utilization using advanced optimization algorithms.

### Key Motivation

While container orchestration tools like Docker Swarm and Kubernetes have gained widespread adoption due to the lightweight nature and efficiency of containers over Virtual Machines (VMs), they have significant limitations:

- **Resource Contention**: Multiple containers on a single node can cause performance degradation due to competing resource demands
- **Lack of Migration Support**: Existing orchestrators don't migrate containers in response to resource contention or security threats
- **Suboptimal Placement**: Static placement decisions don't adapt to changing workload patterns
- **Uneven Resource Utilization**: Variance in resource utilization across cluster nodes remains high

C-Balancer solves these problems through intelligent, periodic container profiling and optimal placement decisions.

## Research Publication & Patent

### Academic Foundation

**Research Paper**: "C-Balancer: A Scheduling Framework for Efficient Container Placement in Clustered Environments"
- **Available at**: https://arxiv.org/abs/2009.08912
- **Published**: May 2025

**Abstract**: Linux containers have gained high popularity in recent times. This popularity is significantly due to various advantages of containers over Virtual Machines (VM). The containers are lightweight, occupy lesser storage, have fast boot-up time, easy to deploy and have faster auto-scaling. The key reason behind the popularity of containers is that they leverage the mechanism of micro-service style software development, where applications are designed as independently deployable services. There are various container orchestration tools for deploying and managing the containers in the cluster. The prominent among them are Docker Swarm and Kubernetes. However, they do not address the effects of resource contention when multiple containers are deployed on a node. Moreover, they do not provide support for container migration in the event of an attack or increased resource contention. To address such issues, C-Balancer provides a scheduling framework for efficient placement of containers in the cluster environment. C-Balancer works by periodically profiling the containers and deciding the optimal container to node placement. Our proposed approach improves the performance of containers in terms of resource utilization and throughput. Experiments using a workload mix of Stress-NG and iPerf benchmark shows that our proposed approach achieves a maximum performance improvement of 58% for the workload mix. Our approach also reduces the variance in resource utilization across the cluster by 60% on average.

### Patent Information

- **Patent Number**: US 12307277
- **Application Number**: 17484396
- **Publication Date**: May 20, 2025
- **Patent Office**: US
- **Inventors**: Dharanipragada Janakiram, Akshay Dhumal

## System Architecture

### Components Overview

C-Balancer consists of several key components that work together to achieve optimal container placement:

#### 1. **Core Components**

##### **Stats (Stats.java)**
- Captures comprehensive statistics for containers and machines
- Tracks metrics including:
  - CPU percentage and usage
  - Memory percentage, usage, and limits
  - Network I/O (inbound/outbound)
  - Block I/O (inbound/outbound)
  - NUMA (Non-Uniform Memory Access) distribution across sockets
  - Normalized metrics for comparison
- Converts various units (B, KiB, MiB, GiB) to a standard format

##### **ClientListener (ClientListener.java)**
- Acts as a distributed listener receiving container statistics from worker nodes
- Consumes statistics from Kafka topics
- Executes migration decisions by invoking container migration scripts
- Manages communication between the scheduler and worker nodes
- Handles container migration to target machines using the local registry

##### **ConsumerLoop (ConsumerLoop.java)**
- Central orchestration component that processes container statistics
- Aggregates real-time statistics from all nodes in the cluster
- Maintains container-to-machine mappings
- Coordinates with the optimization engine
- Logs all operations for monitoring and debugging

##### **MachineStats (MachineStats.java)**
- Represents machine/node information in the cluster
- Associates each machine with a unique identifier and Kafka topic
- Enables multi-channel communication with worker nodes

#### 2. **Optimization Engine**

##### **Genetic Algorithm (GeneticAlgorithm.java)**
- Implements evolutionary computation to find near-optimal container placements
- Key features:
  - **Population-based Search**: Maintains a population of candidate solutions
  - **Multi-objective Optimization**: Balances two metrics:
    - Variance in resource utilization across cluster (minimize)
    - Number of migrations required (minimize)
  - **Mutation Rate**: Controls genetic diversity (default ~0.1)
  - **Fitness Evaluation**: Uses custom metrics to evaluate solution quality
  - **Natural Selection**: Selects the fittest individuals for reproduction

##### **Individual (Individual.java)**
- Represents a single container-to-node placement solution
- **Chromosome**: Array where each index represents a container and the value represents its target node
- **Fitness Score**: Evaluated using variance and migration metrics
- Includes binary search utilities for efficient value lookups

##### **Population (Population.java)**
- Manages a collection of placement solutions (individuals)
- Methods:
  - Generate random population for initial diversity
  - Select fittest individuals (with support for elitism)
  - Calculate overall population fitness
  - Support tournament selection for breeding

#### 3. **Infrastructure Components**

##### **MultiBrokerProducer (MultiBrokerProducer.java)**
- Sends container statistics from worker nodes to the scheduler
- Manages Kafka producer connections to multiple brokers
- Implements partition strategy for load distribution

##### **SimplePartitioner (SimplePartitioner.java)**
- Custom partitioner for Kafka message distribution
- Ensures load balancing across broker instances

#### 4. **Monitoring & Profiling**

##### **read_dockerstats.sh**
- Shell script that collects Docker container statistics from individual nodes
- Runs `docker stats` and parses output
- Executed locally to gather resource metrics
- Output sent to scheduler via Kafka

##### **Stats Collection Flow**
```
Worker Node (read_dockerstats.sh)
    ↓
Container Statistics (CPU, Memory, I/O)
    ↓
MultiBrokerProducer
    ↓
Kafka Brokers
    ↓
ConsumerLoop (Scheduler)
```

## How the Flow Works

### System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                     CLUSTER ENVIRONMENT                         │
├──────────────────┬──────────────────┬──────────────────────────┤
│   Manager Node   │  Worker Node 1   │    Worker Node 2         │
│                  │                  │                          │
│ ┌──────────────┐ │  ┌────────────┐  │  ┌────────────┐          │
│ │  Scheduler   │ │  │ Containers │  │  │ Containers │          │
│ │  (GA Engine) │ │  │ + Stats    │  │  │ + Stats    │          │
│ └──────┬───────┘ │  └────┬───────┘  │  └────┬───────┘          │
│        │         │       │          │       │                 │
│ ┌──────▼──────┐  │  ┌────▼────┐     │  ┌────▼────┐            │
│ │ConsumerLoop │  │  │Producer  │     │  │Producer  │           │
│ └──────┬──────┘  │  └────┬────┘     │  └────┬────┘            │
│        │         │       │          │       │                 │
│ ┌──────▼──────────┴──────┴──────────┴───────┴──────┐           │
│ │      Kafka Broker (Zookeeper, Multiple Brokers)  │           │
│ └───────────────────────────────────────────────────┘           │
│                                                                 │
│ ┌──────────────┐  ┌────────────┐  ┌────────────┐             │
│ │ClientListener│  │Containers  │  │Containers  │             │
│ │  (Migrate)   │──│ Migration   │  │  Profiles  │             │
│ └──────────────┘  └────────────┘  └────────────┘             │
└─────────────────────────────────────────────────────────────────┘
```

### Operational Flow (Step-by-Step)

#### **Phase 1: Profiling & Data Collection**

1. **Periodic Statistics Collection** (Every interval)
   - `read_dockerstats.sh` runs on each worker node
   - Captures: CPU %, Memory %, Network I/O, Block I/O
   - Normalizes values for comparison

2. **Statistics Publishing**
   - `MultiBrokerProducer` sends metrics via Kafka
   - Each node publishes to its assigned Kafka topic
   - `SimplePartitioner` distributes load across brokers

3. **Statistics Aggregation**
   - `ConsumerLoop` receives statistics from all nodes
   - Aggregates metrics per container and per machine
   - Maintains current cluster state

#### **Phase 2: Optimization & Decision Making**

4. **Problem Formulation**
   - Current State: Container A on Node 1, Container B on Node 1, etc.
   - Objective: Find placement that minimizes:
     - **Variance**: Difference in resource utilization across nodes
     - **Migrations**: Number of containers that need to move

5. **Genetic Algorithm Execution**
   - `GeneticAlgorithm` initializes a population of candidate solutions
   - `Population` generates random placements (e.g., [Node2, Node1, Node3, Node2])
   - For each `Individual` (solution):
     - Calculate fitness based on variance and migration costs
     - Lower variance = higher fitness
     - Fewer migrations = higher fitness

6. **Evolution Process** (Multiple generations)
   - **Selection**: Choose fittest individuals using tournament selection
   - **Crossover**: Combine two parent solutions to create offspring
   - **Mutation**: Randomly reassign some containers (controlled by mutation rate)
   - **Replacement**: New generation replaces weakest individuals
   - Converges toward optimal placement

7. **Solution Selection**
   - After N generations, select fittest `Individual`
   - Extract placement decision: which container goes to which node
   - Identify containers that need migration

#### **Phase 3: Container Migration & Implementation**

8. **Migration Decision**
   - `ClientListener` receives the optimal placement
   - For each container needing migration:
     - Execute `migrate_v2.sh` (faster, uses local registry)
     - Or `migrate_v1.sh` (slower, full filesystem migration)

9. **Migration Execution**
   - Container image pulled from registry
   - Container stopped on source node
   - Container started on destination node
   - Network and storage mappings updated

10. **Validation**
    - Verify container is running on new node
    - Resume monitoring statistics on new host

#### **Phase 4: Continuous Monitoring**
- Wait for next collection interval
- Repeat from Phase 1

### Example Scenario

**Initial State**: Cluster has 3 nodes, 4 containers
```
Node 1: Container A (80% CPU, 70% Mem)
Node 1: Container B (75% CPU, 65% Mem)
Node 2: Container C (10% CPU, 15% Mem)
Node 3: Container D (5% CPU, 10% Mem)
Variance: Very High (unbalanced)
```

**GA Optimization**:
```
Generation 1:
  Solution 1: [Node1, Node1, Node2, Node3] - Fitness: 0.45
  Solution 2: [Node1, Node2, Node2, Node3] - Fitness: 0.52
  Solution 3: [Node2, Node3, Node1, Node2] - Fitness: 0.68 ← Best

Generation 2 (based on best solutions):
  Solution 4: [Node2, Node3, Node2, Node1] - Fitness: 0.72
  ...

Generation N:
  Best: [Node1, Node2, Node2, Node3] - Fitness: 0.85
```

**Optimal Placement**:
```
Node 1: Container A
Node 2: Container B, Container C
Node 3: Container D
Variance: Low (balanced)
Migrations Required: 2 (B: 1→2, C: 2→2 no move)
```

**Action Taken**:
```
Migrate Container B from Node 1 to Node 2
Monitor new placement
```

## Key Features & Performance

### Performance Improvements

- **Maximum Performance Improvement**: 58% for workload mix (Stress-NG + iPerf)
- **Variance Reduction**: 60% average reduction in resource utilization variance across cluster
- **Adaptive Placement**: Dynamic rebalancing based on current workload patterns
- **Container Migration**: Support for migrating containers during runtime without service disruption

### Advantages Over Traditional Orchestrators

| Feature | Docker Swarm/Kubernetes | C-Balancer |
|---------|------------------------|-----------|
| Resource Contention Awareness | ❌ No | ✅ Yes |
| Container Migration Support | ❌ Limited | ✅ Full |
| Dynamic Rebalancing | ❌ No | ✅ Yes |
| Variance Optimization | ❌ No | ✅ Yes |
| Workload Profiling | ❌ No | ✅ Yes |
| Intelligent Scheduling | ⚠️ Basic | ✅ Advanced (GA) |

## Installation & Setup

### Prerequisites

- **Operating System**: BOSS MOOL (Kernel Version 3.16) or similar Linux distribution
- **Docker**: Version 1.13.0+ (with Experimental features enabled)
- **Kafka**: Version 2.11-0.9.0.0
- **Zookeeper**: Compatible with Kafka 0.9.0.0
- **CRIU**: 3.4+ (for container migration)
- **Java**: JDK 1.8.0_144+
- **Network**: All nodes must have proper internet connectivity

### Installation Steps

1. **Clone the Repository**
   ```bash
   git clone <repository_url>
   cd ConBalancer/Scheduler
   ```

2. **Install Docker**
   ```bash
   sh ./install_docker.sh
   docker version  # Verify installation
   ```

3. **Install C-Balancer**
   ```bash
   sudo sh ./install.sh
   ```

4. **Verify CRIU Installation**
   ```bash
   systemctl show  # Check CRIU in ENVIRONMENT
   criu  # Test CRIU command
   ```

5. **Move to Production Location**
   ```bash
   mv /home/<hostname>/ConBalancer/Scheduler /opt
   cd /opt
   tar -zxvf kafka_updated.tar
   ```

6. **Configure Nodes**
   - Edit `user_ips` - List of all worker node IP addresses
   - Edit `user_name` - Corresponding usernames for SSH access
   - Execute SSH setup: `./ssh-all.sh`
   - Add nodes to `/etc/hosts` file

7. **Setup Container Registry** (for fast migration)
   ```bash
   mkdir -p certs
   openssl req -newkey rsa:4096 -nodes -sha256 \
     -keyout certs/domain.key -x509 -days 365 -out certs/domain.crt
   
   docker run -d --name registry \
     -v /home/<hostname>/certs:/certs \
     -e REGISTRY_HTTP_ADDR=0.0.0.0:443 \
     -e REGISTRY_HTTP_TLS_CERTIFICATE=/certs/domain.crt \
     -e REGISTRY_HTTP_TLS_KEY=/certs/domain.key \
     -p 443:443 registry:2
   ```

8. **Start Zookeeper & Kafka**
   ```bash
   # On each node
   ./start_zk.sh
   ./start_kafka.sh
   ```

## Usage Guide

### Starting the Framework

1. **Start Listeners on All Worker Nodes**
   ```bash
   ./start_listeners.sh
   ```
   The listeners wait for migration commands from the scheduler.

2. **Start Producers/Stats Collection**
   ```bash
   ./start_producers.sh
   ```
   Collects and publishes container statistics to Kafka.

3. **Run Containers**
   ```bash
   ./run.sh  # Configure before running
   ```
   Deploys containers on worker nodes.

4. **Start the Optimizer/Scheduler**
   ```bash
   cd src
   /opt/java/jdk1.8.0_144/bin/java \
     -Dfile.encoding=UTF-8 \
     -classpath /path/to/libs \
     ConsumerLoop
   ```
   The scheduler begins collecting statistics and optimizing placement.

### Managing the System

#### View Container Statistics
```bash
./read_dockerstats.sh
```

#### View System Logs
```bash
./read_logs.sh
```

#### Stop All Operations
```bash
./stop_all.sh          # Stop all services
./stop_listeners.sh    # Stop only listeners
./stop_producers.sh    # Stop only statistics collection
./stopcon_global.sh    # Stop and remove containers
```

#### Clean Up
```bash
./delete_logs.sh       # Remove all logs
./complete_reset.sh    # Full system reset
```

## Comprehensive Script Documentation

### Core Infrastructure Setup

#### `install_docker.sh`
**Purpose**: Install Docker Engine 1.13.0 with experimental features  
**What It Does**:
- Installs Docker from official repository (specific version required)
- Enables experimental mode for checkpoint/restore functionality
- Configures devicemapper storage driver
- Sets up insecure registry support for local testing
- Grants socket permissions for container management
- **Duration**: 5-10 minutes

**Usage**: `./install_docker.sh`  
**When to Use**: First-time setup on any node

---

#### `install.sh`
**Purpose**: Compile and install CRIU (Checkpoint-Restore in Userspace) 3.4  
**What It Does**:
- Installs system dependencies (protobuf, netlink, capabilities libraries)
- Downloads CRIU 3.4 source code
- Compiles CRIU from source (required for container checkpointing)
- Adds CRIU binary to system PATH
- Configures environment variables
- **Duration**: 10-20 minutes (compilation-intensive)

**Usage**: `sudo ./install.sh` (edit `id` variable first for multi-node setup)  
**When to Use**: Once per node, before starting C-Balancer

---

### Zookeeper & Kafka Cluster Management

#### `start_zk.sh`
**Purpose**: Start Zookeeper server  
**What It Does**:
- Launches Zookeeper instance on this machine
- Enables cluster coordination and broker discovery
- Required before starting Kafka brokers
- Runs in background

**Usage**: `./start_zk.sh`  
**When to Use**: Before starting Kafka; run on all Zookeeper-designated nodes  
**Verification**: `zkServer.sh status` shows "Mode: standalone/leader/follower"

---

#### `start_kafka.sh`
**Purpose**: Start Kafka broker server  
**What It Does**:
- Launches Kafka broker instance
- Forms part of multi-broker cluster
- Hosts Kafka topics for statistics/migration communication
- Runs in background, requires Zookeeper

**Usage**: `./start_kafka.sh` (ensure Zookeeper is running first)  
**When to Use**: After Zookeeper on all Kafka broker nodes  
**Verification**: `kafka-broker-api-versions.sh` shows broker connectivity

---

#### `stop_all.sh`
**Purpose**: Gracefully stop all Kafka and Zookeeper services  
**What It Does**:
- Finds and terminates all Kafka broker processes (SIGTERM)
- Waits 1 second for graceful shutdown  
- Force-kills remaining Zookeeper processes (SIGKILL)
- Cleans up process IDs

**Usage**: `./stop_all.sh`  
**When to Use**: Before shutdown, maintenance, or complete system reset

---

### Container Statistics & Monitoring

#### `start_producers.sh`
**Purpose**: Deploy statistics producers on all worker nodes  
**What It Does**:
- Reads machine configuration (usernames and IPs)
- SSH connects to each worker node
- Executes `read_dockerstats.sh` on each node (periodic task)
- Publishes collected statistics to Kafka topics
- Enables central scheduler to receive real-time metrics
- **Metrics Collected**: CPU%, Memory%, Network I/O, Block I/O, NUMA distribution

**Configuration Files**:
- `user_name` - One username per line (in order)
- `user_ips` - One IP address per line (must match user_name order)

**Usage**: 
```bash
./start_producers.sh          # Default: 6-second collection interval
./start_producers.sh 10       # Custom: 10-second collection interval
```

**When to Use**: After starting Kafka; run once on manager node  
**Verification**: `kafka-console-consumer.sh` shows statistics flowing into topics

---

#### `stop_producers.sh`
**Purpose**: Stop statistics collection on all nodes  
**What It Does**:
- SSH connects to all nodes
- Kills `read_dockerstats.sh` processes
- Stops statistical data publication
- Useful for debugging or reconfiguration

**Usage**: `./stop_producers.sh`  
**When to Use**: Before reconfiguration or shutdown

---

#### `read_dockerstats.sh`
**Purpose**: Collect local container statistics (runs on each node)  
**What It Does**:
- Executes `docker stats` command
- Extracts CPU, memory, network, and block I/O metrics
- Normalizes values to standard units (MiB, %)
- Publishes data to Kafka via MultiBrokerProducer
- Typically invoked by `start_producers.sh` in a loop

**Metrics Format**:
```
timestamp, containerID, cpu%, mem%, net_in/out, block_in/out, numa_dist...
```

**Usage**: `./read_dockerstats.sh` (typically called via start_producers.sh)  
**Frequency**: Configurable (default 6 seconds)

---

#### `collect_stats.sh`
**Purpose**: Aggregate statistics from all nodes to a local file  
**What It Does**:
- Connects to all worker nodes via SSH
- Collects local statistics
- Aggregates into centralized report file
- Useful for batch analysis and debugging

**Output**: Statistics file with timestamp at `/opt/kafka_2.11-0.9.0.0/topics_HHMI.txt`

---

#### `read_logs.sh`
**Purpose**: Display system operation logs  
**What It Does**:
- Retrieves and displays C-Balancer operation logs
- Shows optimization decisions and migrations
- Useful for monitoring and troubleshooting

**Usage**: `./read_logs.sh`

---

### Container Deployment

#### `run.sh`
**Purpose**: Deploy test workload containers  
**What It Does**:
- Launches Docker containers with CPU-intensive workloads
- Multiple test workload options (CPU, network, readahead, crypto, bsearch)
- Currently active: 2x CPU stress containers (matrix product, 300s each)
- Other workloads commented out for selection

**Active Workloads**:
- **CPU Stress**: Matrix computation on 2 CPU cores
- **Network Stress** (optional): iPerf bandwidth testing
- **Readahead** (optional): Cache performance testing
- **Cryptography** (optional): Crypto operation stress
- **Binary Search** (optional): Memory-intensive operations

**Customization**: Edit script to uncomment desired workloads  
**Usage**: `./run.sh`  
**Verification**: `docker ps` shows running containers; `docker stats` shows metrics

---

#### `runcontainers.sh`
**Purpose**: Deploy containers on all worker nodes simultaneously  
**What It Does**:
- SSH connects to each worker node
- Executes container deployment on each node
- Distributed workload across cluster

**Usage**: `./runcontainers.sh`

---

#### `stopcon_global.sh`
**Purpose**: Stop and remove all containers cluster-wide  
**What It Does**:
- Connects to all worker nodes
- Stops running containers
- Removes container instances completely

**Usage**: `./stopcon_global.sh`  
**Warning**: Destructive operation - removes container data

---

### Container Migration

#### `migrate_v2.sh`
**Purpose**: Fast container migration using local Docker registry  
**What It Does**:
1. Extracts container image ID and command line
2. Creates CRIU checkpoint (state preservation)
3. Creates filesystem archive
4. Commits container image to registry
5. Pushes image and checkpoints to destination
6. Stops source container
7. Pulls image on destination and restores from checkpoint
8. Starts container on destination node

**Performance**: ~50% faster than v1 (uses local registry optimization)

**Parameters**:
- `$1` - Container ID to migrate
- `$2` - Destination machine node ID
- `$3` - Registry address (e.g., sparknode19:443/)

**Usage**: `./migrate_v2.sh container_id destination_id registry_address`  
**Prerequisites**: 
- CRIU installed on both nodes
- Registry running and accessible
- SSH access to destination

---

#### `migrate_v1.sh`
**Purpose**: Full container migration with filesystem copy  
**What It Does**:
- Similar to v2 but performs full filesystem transfer
- No registry optimization
- Slower but more portable

**Usage**: `./migrate_v1.sh container_id destination_id`  
**Performance**: Slower than v2 (complete filesystem copy)

---

#### `transfer.sh`
**Purpose**: Transfer container images between nodes  
**What It Does**:
- Copies Docker images from source to destination via `docker push/pull`
- Useful for pre-staging images on nodes

---

### Scheduler & Optimization

#### `run_scheduler.sh`
**Purpose**: Start the central C-Balancer scheduler (Genetic Algorithm engine)  
**What It Does**:
1. SSH connects to scheduler node
2. Executes ConsumerLoop (main orchestrator)
3. Starts receiving container statistics from Kafka
4. Runs Genetic Algorithm for placement optimization
5. Publishes migration commands when beneficial
6. Repeats optimization in configurable cycles

**What It Optimizes**:
- Variance in resource utilization across nodes (lower is better)
- Number of migrations required (fewer is better)

**Usage**: `./run_scheduler.sh`  
**Frequency**: Runs continuously; optimization cycles every 5-60 seconds (configurable)

---

### Development & Testing

#### `swarm_test.sh`
**Purpose**: Run Docker Swarm cluster tests  
**What It Does**:
- Tests Docker Swarm orchestration features
- Used for comparison with C-Balancer

**Usage**: `./swarm_test.sh`

---

#### `swarm_stop.sh`
**Purpose**: Stop Docker Swarm testing resources  
**Usage**: `./swarm_stop.sh`

---

### System Maintenance

#### `ssh-all.sh`
**Purpose**: Establish SSH connectivity to all worker nodes  
**What It Does**:
- Tests SSH access to all configured nodes
- Sets up SSH key-based authentication
- Verifies network connectivity

**When to Use**: After initial cluster setup; before running production scripts

---

#### `sys_update.sh`
**Purpose**: Update system packages on all nodes  
**What It Does**:
- Runs `apt-get update` and `apt-get upgrade` on all nodes
- Keeps system software current

---

#### `update_on_restart.sh`
**Purpose**: Configure automatic updates on service restart  
**What It Does**:
- Ensures updates are applied when services restart
- Maintains consistent system state

---

#### `delete_logs.sh`
**Purpose**: Remove all C-Balancer operation logs  
**What It Does**:
- Clears log files from all nodes
- Useful for starting fresh or resetting diagnostics
- **Warning**: Removes historical data about operations

**Usage**: `./delete_logs.sh`

---

#### `complete_reset.sh`
**Purpose**: Complete system reset to fresh state  
**What It Does**:
- Stops all services (Kafka, Zookeeper, containers)
- Removes all data and logs
- Clears Kafka topics
- Resets system configuration
- **Warning**: Destructive - removes all data

**Usage**: `./complete_reset.sh`  
**When to Use**: Starting completely fresh or debugging issues

---

#### `dis_initiate.sh`
**Purpose**: Kill Zookeeper and Kafka servers on one system  
**Notes**: Requires configuration before use

---

## Source Code Component Documentation

### Java Classes

#### `GeneticAlgorithm.java`
**Role**: Optimization engine for container placement  
**Key Methods**:
- `initPopulation()` - Create initial solution population
- `getMean()` - Calculate average resource utilization
- `calculateMigrationCount()` - Count required migrations
- `calcFitness()` - Evaluate solution quality (lower is better)
- Inner class `OptimizationMetric` - Tracks variance and migration costs

**Algorithm**: Genetic Algorithm with multi-objective optimization

---

#### `ConsumerLoop.java`
**Role**: Main scheduler orchestrator  
**Key Methods**:
- `add_info()` - Receive and aggregate statistics from Kafka
- `run()` - Main event loop polling for statistics
- Inner classes:
  - `ContainerInfo` - Full container metadata
  - `ContainerStatsWrapper` - Statistics wrapper

**Responsibilities**:
1. Consume statistics from all Kafka topics
2. Maintain current container-to-machine mappings
3. Invoke Genetic Algorithm
4. Publish migration commands

---

#### `ClientListener.java`
**Role**: Worker node migration executor  
**Key Methods**:
- `executeContainerMigration()` - Execute migration script
- `run()` - Main listener loop (polls for migration commands)
- `main()` - Initialization and thread pool setup

**Responsibilities**:
1. Listen for migration commands on Kafka topic
2. Parse container ID and destination
3. Execute migration script
4. Report status

---

#### `MultiBrokerProducer.java`
**Role**: Statistics publisher to Kafka  
**Key Methods**:
- `produce()` - Send statistics message to topic

**Function**: Publishes container statistics from workers to scheduler

---

#### `SimplePartitioner.java`
**Role**: Kafka message partitioner  
**Algorithm**: Modulo-based partitioning for deterministic distribution  
**Benefit**: Ensures consistent routing and load distribution

---

#### `Stats.java`
**Role**: Container statistics data container  
**Fields**: CPU%, memory%, network I/O, block I/O, NUMA data  
**Key Methods**: `convertStats()` - Parse and normalize metrics

---

#### `Individual.java`
**Role**: Single placement solution for genetic algorithm  
**Representation**: Chromosome array where index=container, value=target node  
**Key Methods**: `getGene()`, `setGene()` - Access chromosome data

---

#### `Population.java`
**Role**: Collection of placement solutions  
**Key Methods**:
- `getFittest()` - Select best solutions
- Sorting and comparison for evolutionary selection

---

## Variable Naming Improvements

All Java files have been updated with descriptive variable names:
- `hm` → `machineContainersMap`
- `c_info` → `allContainersInfo`/`currentContainerInfo`
- `hm_machine_ips` → `machineAddressMap`
- `con_info` → `containerEntry`
- `metric` → `OptimizationMetric`
- `mutationRate`, `crossoverRate`, `elitismCount` - Added full documentation
- Function parameters use full descriptive names (e.g., `machineIds` instead of `machine_ids`)

All classes and methods now include comprehensive JavaDoc documentation.

## Project Structure

```
ConBalancer/
├── README.md                          # This file
├── Scheduler/
│   ├── src/
│   │   ├── GeneticAlgorithm.java      # GA optimizer engine
│   │   ├── Individual.java            # Placement solution
│   │   ├── Population.java            # Solution population
│   │   ├── ConsumerLoop.java          # Main orchestrator
│   │   ├── ClientListener.java        # Statistics consumer
│   │   ├── MachineStats.java          # Node representation
│   │   ├── MultiBrokerProducer.java   # Statistics publisher
│   │   ├── SimplePartitioner.java     # Kafka partitioner
│   │   ├── Stats.java                 # Statistics container
│   │   └── read_dockerstats.sh        # Local stats collector
│   ├── Management Scripts/
│   │   ├── install.sh / install_docker.sh
│   │   ├── start_*.sh / stop_*.sh
│   │   ├── migrate_v*.sh
│   │   └── ... (additional utility scripts)
│   └── Configuration Files/
│       ├── user_ips
│       └── user_name
```

## Version Information

- **Docker**: 1.13.0, build 49bf474
- **Kafka**: 2.11-0.9.0.0
- **Zookeeper**: 3.4.6
- **CRIU**: 3.4+
- **Java**: 1.8.0_144+

## Troubleshooting

### Common Issues

1. **Docker experimental features not enabled**
   - Edit `/etc/docker/daemon.json`
   - Set `"experimental": true`
   - Restart Docker: `systemctl restart docker`

2. **Permission errors on `/var/run/docker.sock`**
   ```bash
   chmod 777 /var/run/docker.sock
   ```

3. **Container migration fails**
   - Verify CRIU installation: `criu --version`
   - Check network connectivity between nodes
   - Ensure registry is running and accessible

4. **Kafka connection issues**
   - Verify Zookeeper is running: `zkServer.sh status`
   - Check Kafka broker: `./bin/kafka-broker-api-versions-client.sh`
   - Verify bootstrap servers in configuration

## Future Enhancements

- Support for Kubernetes orchestration
- Advanced ML-based placement predictions
- Multi-objective optimization with more metrics
- Real-time anomaly detection and automatic migration
- Web-based monitoring dashboard
- Automated energy-aware placement

## Citation

If you use C-Balancer in your research or systems, please cite:

```bibtex
@article{CBalancer2025,
  title={C-Balancer: A Scheduling Framework for Efficient Container Placement in Clustered Environments},
  author={Janakiram, Dharanipragada and Dhumal, Akshay},
  journal={arXiv preprint arXiv:2009.08912},
  year={2025}
}
```

## Patent Reference

- **US Patent**: 12,307,277
- **Application**: 17,484,396
- **Inventors**: Dharanipragada Janakiram, Akshay Dhumal

## License

[Add appropriate license information]

## Contact & Support

For questions, issues, or contributions, please contact the authors or open an issue in the repository.
