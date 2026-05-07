#!/bin/bash
#
# RUN SCHEDULER - START C-BALANCER OPTIMIZATION ENGINE
#
# Description:
#   Remotely starts the C-Balancer scheduler on the manager/scheduler node.
#   This is the central component that orchestrates container placement optimization.
#
# Processing:
#   1. Connects via SSH to manager node (gas@10.21.229.203)
#   2. Authenticates with password
#   3. Executes the optimization engine (ConsumerLoop and GeneticAlgorithm)
#
# What the Scheduler Does:
#   1. Consumes container statistics from Kafka topics
#   2. Aggregates metrics from all nodes
#   3. Runs Genetic Algorithm to optimize placement
#   4. Publishes migration commands via Kafka to ClientListeners
#   5. Repeats optimization in configurable cycles
#
# Prerequisites:
#   - SSH access to scheduler node (gas@10.21.229.203)
#   - Zookeeper and Kafka running
#   - All worker listeners and producers started
#   - Java and C-Balancer installed on scheduler node
#
# Usage:
#   ./run_scheduler.sh
#
# See Also:
#   - ConsumerLoop.java: Main scheduler logic
#   - GeneticAlgorithm.java: Optimization engine
#   - start_listeners.sh: Start migration listeners
#   - start_producers.sh: Start statistics producers
#

machine=gas@10.21.229.203
ssh $machine "echo akshay1206 | /home/gas/Projects/ConBalancer/Scheduler/run.sh" 
