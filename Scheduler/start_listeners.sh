#!/bin/bash
#
# START MIGRATION LISTENERS ON ALL WORKER NODES
#
# Description:
#   Deploys and starts ClientListener instances on all worker nodes in the cluster.
#   These listeners wait for migration commands from the central scheduler.
#   Each listener subscribes to its machine's dedicated Kafka topic.
#
# Configuration:
#   Reads from files:
#   - user_name: One username per line for each worker node
#   - user_ips: One IP address per line for each worker node (must match user_name)
#
# Processing:
#   1. Loads machine usernames and IP addresses from configuration files
#   2. For each worker node:
#      - SSH connects to the node
#      - Starts ClientListener process
#      - Listener subscribes to machine-specific Kafka topic
#   3. All listeners run in the background on their respective nodes
#
# Usage:
#   ./start_listeners.sh
#
# Prerequisites:
#   - user_name and user_ips files must be configured
#   - SSH key-based authentication must be set up for all nodes
#   - Java must be installed on all worker nodes
#   - Kafka and C-Balancer must be installed on all nodes
#
# See Also:
#   - ./stop_listeners.sh: Stop all listeners
#   - ./start_producers.sh: Start statistics producers
#

IFS=$'\n' read -d '' -r -a user_name < user_name
IFS=$'\n' read -d '' -r -a user_ips < user_ips

at_rate="@"
topic_m="L"



echo "Listeener Machine Names"
for i in "${!user_name[@]}"
do
        mach_name[$i]=${user_name[i]}$at_rate${user_ips[i]}
        #echo ${mach_name[i]}
done
#Display all the machine names 
cmdline_ip=""
space=" "
for i  in "${!mach_name[@]}"
do
        cmdline_ip=$cmdline_ip$space${mach_name[i]}
done

echo "CMD LINE " $cmdline_ip


for i  in "${!mach_name[@]}"
do
        topic=$topic_m$i
        echo "Listener Machine is " ${mach_name[i]} 
        ssh -t  ${mach_name[i]}  "echo boss |sudo -S   /opt/kafka_2.11-0.9.0.0/run_client.sh $cmdline_ip $i" &
  
        #sleep 1
done

