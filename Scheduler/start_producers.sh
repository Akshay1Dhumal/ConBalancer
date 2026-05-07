#!/bin/bash
#
# START CONTAINER STATISTICS PRODUCERS ON ALL WORKER NODES
#
# Description:
#   Deploys and starts MultiBrokerProducer instances on all worker nodes.
#   These producers continuously collect container statistics (CPU, memory, I/O)
#   and publish them to Kafka topics for consumption by the central scheduler.
#
# Configuration:
#   Reads from files:
#   - user_name: One username per line for each worker node
#   - user_ips: One IP address per line for each worker node
#
# Statistics Collected (per collection cycle):
#   - CPU percentage and usage
#   - Memory percentage, usage, and limits
#   - Network I/O (inbound/outbound)
#   - Block I/O (inbound/outbound)
#   - NUMA memory distribution
#
# Processing:
#   1. Loads machine usernames and IP addresses from configuration files
#   2. Configures collection period (default: 6 seconds)
#   3. For each worker node:
#      - SSH connects and executes read_dockerstats.sh
#      - Collects local container statistics
#      - Publishes to Kafka via MultiBrokerProducer
#      - Runs in background on each node
#
# Usage:
#   ./start_producers.sh [period]
#   Examples:
#     ./start_producers.sh        # Uses default period (6 seconds)
#     ./start_producers.sh 10     # Collect every 10 seconds
#
# Prerequisites:
#   - user_name and user_ips must be configured
#   - SSH key-based authentication configured
#   - Docker must be installed on all worker nodes
#   - Kafka brokers must be running
#
# Output:
#   - Statistics continuously published to Kafka
#   - Logs stored at /opt/kafka_2.11-0.9.0.0/topics_HHMI.txt
#
# See Also:
#   - ./stop_producers.sh: Stop statistics collection
#   - ./read_dockerstats.sh: Local statistics collection script
#   - ./start_listeners.sh: Start migration listeners
#

IFS=$'\n' read -d '' -r -a user_name < user_name
IFS=$'\n' read -d '' -r -a user_ips < user_ips
at_rate="@"
topic_m="M"

period=$1
period=6 #Default period : ie 6 seconds
echo "Collection Period (seconds): "$period


#Logs
fname=topics_"$(date +%H%M)".txt
fpath="/opt/kafka_2.11-0.9.0.0/"
fname=$fath$fname
echo $fname
#broker-lits here Set the IP here of the kafka brokers
#ips_b=(10.21.235.181 10.21.233.193)


broker_port=":9092,"
brokers=""
for i in "${ips_b[@]}"
do
        c=$i$broker_port
        brokers=$brokers$c
done
br_length=$(echo -n $brokers | wc -m)
brokers=${brokers:0:($br_length-1)}
echo "Kafka-Broker IP"
echo $brokers

#Zookeeper list here
#ips_z=(10.21.235.181 10.21.233.193)
z_port=":2181,"
zooks=""
for i in "${ips_z[@]}"
do
        c=$i$z_port
        zooks=$zooks$c
done
zk_length=$(echo -n $zooks | wc -m)
zooks=${zooks:0:($zk_length-1)}
echo "Zookeeper IP"
echo $zooks 





echo "Machine Names"
for i in "${!user_name[@]}"
do
	mach_name[$i]=${user_name[i]}$at_rate${user_ips[i]}
        #echo ${mach_name[i]}
done
#Display all the machine names
for i  in "${!mach_name[@]}"
do
	echo ${mach_name[i]}
done


#Stat producers on all machines

for i  in "${!mach_name[@]}"
do
	topic=$topic_m$i
	echo "Machine and Topic is " ${mach_name[i]} $topic
	#echo "Machine and Topic is " ${mach_name[i]} $topic >> fname	
	ssh   ${mach_name[i]}  "echo boss | /opt/kafka_2.11-0.9.0.0/read_dockerstats.sh $period | /opt/kafka_2.11-0.9.0.0/bin/kafka-console-producer.sh --broker-list $brokers --topic $topic" &
	#sleep 1
done


#ssh -t  sparknode17@10.21.233.193  "echo boss | /opt/kafka_2.11-0.9.0.0/read_dockerstats.sh 6 | /opt/kafka_2.11-0.9.0.0/bin/kafka-console-producer.sh --broker-list $brokers --topic M0"

#ssh -t  sparknode17@10.21.235.181  "echo boss | /opt/kafka_2.11-0.9.0.0/read_dockerstats.sh 6 | /opt/kafka_2.11-0.9.0.0/bin/kafka-console-producer.sh --broker-list $brokers --topic M1"


