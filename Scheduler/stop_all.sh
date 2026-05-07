#!/bin/bash
#
# STOP ALL SERVICES
#
# Description:
#   Stops all C-Balancer infrastructure services:
#   - Kafka broker processes
#   - Zookeeper server
#
# Processing:
#   1. Finds and kills all Kafka broker processes (SIGTERM)
#   2. Waits 1 second
#   3. Finds and kills all Zookeeper processes (SIGKILL)
#
# Usage:
#   ./stop_all.sh
#
# Notes:
#   - Use ./stop_listeners.sh to stop only the migration listeners
#   - Use ./stop_producers.sh to stop only the statistics producers
#   - For data cleanup, run ./delete_logs.sh after stopping services
#

# Stop Kafka brokers gracefully with SIGTERM
ps ax | grep -i 'kafka\.Kafka' | grep java | grep -v grep | awk '{print $1}' | xargs kill -SIGTERM
sleep 1

# Force kill Zookeeper if still running
ps ax | grep -i 'zookeeper' | grep -v grep | awk '{print $1}' | xargs kill -SIGKILL

# For removing data use the delete_logs.sh file
