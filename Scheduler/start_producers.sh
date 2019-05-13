#!/bin/bash
#Machine username and IP set here

IFS=$'\n' read -d '' -r -a user_name < user_name
IFS=$'\n' read -d '' -r -a user_ips < user_ips




#user_name=(sparknode17 sparknode17 sparknode19 sparknode18 sparknode18)
#user_ips=(10.21.235.181 10.21.233.193 10.21.234.54 10.21.239.216 10.21.239.161)
at_rate="@"
topic_m="M"


period=$1
period=6 #Default period : ie 1 sec
echo "Period is "$period
#ZOOKEEPER IPS SET HERE
ips_z=(10.21.235.181 10.21.233.193)
#KAFKA-BROKERS SET HERE
ips_b=(10.21.235.181 10.21.233.193)


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


