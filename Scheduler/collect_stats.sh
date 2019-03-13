#topic="stats1" #Provide a unique topic for each machine
#echo "Starting stat collection using TOPIC : " $topic
#docker stats --format "table {{.Container}}: {{.CPUPerc}}: {{.MemUsage}}: {{.MemPerc}}: {{.NetIO}}: {{.BlockIO}}" | ./bin/kafka-console-producer.sh --broker-list sparknode19:9092,sparknode20:9092,gas:9092 --topic $topic

#!/bin/bash
period=6 #Default period : ie 1 sec
period=$1
#ZOOKEEPER IPS SET HERE
ips_z=(10.21.235.181 10.21.233.193)
#KAFKA-BROKERS SET HERE
ips_b=(10.21.235.181 10.21.233.193)


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
echo "KAFKA-BROKER IPS"
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
echo "ZOOKEEPER IPS"
echo $zooks




#Create topic first
#x=$(./bin/kafka-topics.sh --create --zookeeper $zooks --replication-factor 1  --partitions 1 --topic mach1)
#x=$(./bin/kafka-topics.sh --create --zookeeper $zooks --replication-factor 1  --partitions 1 --topic mach2)
#Listing topics 

#./bin/kafka-topics.sh --list --zookeeper $zooks & 


#ssh -t  sparknode18@10.21.239.161  "echo boss |sudo -S  . ./opt/kafka_2.11-0.9.0.0/read_dockerstats.sh 6 | /opt/kafka_2.11-0.9.0.0/bin/kafka-console-producer.sh --broker-list sparknode19:9092,sparknode18:9092,gas:9092 --topic mach2"
#ssh  -t  sparknode19@10.21.235.8 "echo boss |sudo -S . /opt/kafka_2.11-0.9.0.0/read_dockerstats.sh 6 | /opt/kafka_2.11-0.9.0.0/bin/kafka-console-producer.sh --broker-list sparknode19:9092,sparknode18:9092,gas:9092 --topic mach1"
#ssh -t gas@10.21.229.203 "echo manipraju | sudo -S . /opt/kafka_2.11-0.9.0.0/read_dockerstats.sh 6  | /opt/kafka_2.11-0.9.0.0/bin/kafka-console-producer.sh --broker-list sparknode19:9092,sparknode18:9092,gas:9092 --topic mach3" 

ssh -t  sparknode17@10.21.233.193  "echo boss | /opt/kafka_2.11-0.9.0.0/read_dockerstats.sh 6 | /opt/kafka_2.11-0.9.0.0/bin/kafka-console-producer.sh --broker-list $brokers --topic M1"

ssh -t  sparknode17@10.21.235.181  "echo boss | /opt/kafka_2.11-0.9.0.0/read_dockerstats.sh 6 | /opt/kafka_2.11-0.9.0.0/bin/kafka-console-producer.sh --broker-list $brokers --topic M2"



