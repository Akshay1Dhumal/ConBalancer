#!/bin/bash
#
# START ZOOKEEPER SERVER
#
# Description:
#   Starts the Zookeeper server instance on this machine.
#   Zookeeper is required for Kafka cluster coordination and broker discovery.
#   The server runs in the background.
#
# Prerequisites:
#   - Kafka installation present at /opt/kafka_2.11-0.9.0.0/
#   - zookeeper.properties configured at /opt/kafka_2.11-0.9.0.0/config/
#   - Java installed and in PATH
#
# Usage:
#   ./start_zk.sh
#
# Output:
#   Zookeeper logs will be displayed in the terminal
#   Server runs in background
#

/opt/kafka_2.11-0.9.0.0/bin/zookeeper-server-start.sh /opt/kafka_2.11-0.9.0.0/config/zookeeper.properties &
