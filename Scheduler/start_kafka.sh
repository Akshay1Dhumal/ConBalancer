#!/bin/bash
#
# START KAFKA BROKER SERVER
#
# Description:
#   Starts a Kafka broker instance on this machine.
#   Kafka brokers form the messaging layer for publishing/consuming container statistics.
#   Each cluster requires multiple brokers for high availability.
#   The server runs in the background.
#
# Prerequisites:
#   - Zookeeper must be running before starting Kafka
#   - Kafka installation present at /opt/kafka_2.11-0.9.0.0/
#   - server.properties configured at /opt/kafka_2.11-0.9.0.0/config/
#   - Java installed and in PATH
#
# Usage:
#   ./start_zk.sh   # Start Zookeeper first
#   ./start_kafka.sh # Then start Kafka broker
#
# Output:
#   Kafka logs will be displayed in the terminal
#   Server runs in background
#

#!/bin/bash
/opt/kafka_2.11-0.9.0.0/bin/kafka-server-start.sh  /opt/kafka_2.11-0.9.0.0/config/server.properties &
