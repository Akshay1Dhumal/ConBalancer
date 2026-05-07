#!/bin/bash
#
# STOP CONTAINER STATISTICS PRODUCERS
#
# Description:
#   Stops all statistics producer processes on all worker nodes.
#   This halts the collection and publication of container metrics to Kafka.
#
# Action Taken:
#   1. Reads machine configuration (usernames and IPs)
#   2. SSH connects to each worker node
#   3. Finds and terminates read_dockerstats.sh processes
#   4. Stops Kafka statistics publishing
#
# Configuration:
#   - user_name: Worker node usernames
#   - user_ips: Worker node IP addresses
#
# Metrics Stopped:
#   - CPU percentage and usage
#   - Memory percentage and usage
#   - Network I/O metrics
#   - Block I/O metrics
#
# Usage:
#   ./stop_producers.sh
#
# Prerequisites:
#   - SSH access to all nodes
#   - Producers must be running (started by start_producers.sh)
#
# When to Use:
#   - Before system shutdown
#   - Before reconfiguring collection parameters
#   - For debugging or testing
#
# See Also:
#   - ./start_producers.sh: Start statistics collection
#   - ./read_dockerstats.sh: Local stats collection script
#   - ./stop_listeners.sh: Stop migration listeners
#
# Machine username and IP set here
#user_name=(sparknode17 sparknode17)
#user_ips=(10.21.235.181 10.21.233.193)

IFS=$'\n' read -d '' -r -a user_name < user_name
IFS=$'\n' read -d '' -r -a user_ips < user_ips

at_rate="@"
topic_m="M"


#ZOOKEEPER IPS SET HERE
ips_z=(10.21.235.181 10.21.233.193)
#KAFKA-BROKERS SET HERE
ips_b=(10.21.235.181 10.21.233.193)


#Logs
fname=MyFile_"$(date +%Y%d%M)".txt
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


#Stopping producers on all machines

for i  in "${!mach_name[@]}"
do
	echo "Stopping producer " ${mach_name[i]}  
	ssh -t  ${mach_name[i]}  "echo boss | sudo -S /opt/kafka_2.11-0.9.0.0/stop_producer_local.sh " &
#	ssh -t  ${mach_name[i]}  "echo boss | /opt/kafka_2.11-0.9.0.0/read_dockerstats.sh $period | /opt/kafka_2.11-0.9.0.0/bin/kafka-console-producer.sh --broker-list $brokers --topic $topic"
done


#ssh -t  sparknode17@10.21.233.193  "echo boss | /opt/kafka_2.11-0.9.0.0/read_dockerstats.sh 6 | /opt/kafka_2.11-0.9.0.0/bin/kafka-console-producer.sh --broker-list $brokers --topic M1"

