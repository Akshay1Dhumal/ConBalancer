#!/bin/bash
#Machine username and IP set here for zookeeper and kafka nodes
user_name=(sparknode17 sparknode17)
user_ips=(10.21.235.181 10.21.233.193)
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





echo "Machine Names of Zookeeper and kafka nodes"
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


#Starting Zookeeper on all machines
for i  in "${!mach_name[@]}"
do
	echo "Starting Zookeeper on"${mach_name[i]} 	
	ssh -t ${mach_name[i]}  "echo boss | /opt/kafka_2.11-0.9.0.0/start_zk.sh" &
done

sleep 5

#Starting Kafka on all machines

for i  in "${!mach_name[@]}"
do
	echo "Staring kafka server broker" ${mach_name[i]} 
        ssh -t ${mach_name[i]}  "echo boss | /opt/kafka_2.11-0.9.0.0/start_kafka.sh" &
done


#for i  in "${!mach_name[@]}"
#do
#	topic=$topic_m$i
#	ssh -t  ${mach_name[i]}  "echo boss | /opt/kafka_2.11-0.9.0.0/read_dockerstats.sh $period | /opt/kafka_2.11-0.9.0.0/bin/kafka-console-producer.sh --broker-list $brokers --topic $topic"
#done


#ssh -t  sparknode17@10.21.233.193  "echo boss | /opt/kafka_2.11-0.9.0.0/read_dockerstats.sh 6 | /opt/kafka_2.11-0.9.0.0/bin/kafka-console-producer.sh --broker-list $brokers --topic M1"

