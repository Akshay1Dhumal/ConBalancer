#topic="stats1" #Provide a unique topic for each machine
#echo "Starting stat collection using TOPIC : " $topic
#docker stats --format "table {{.Container}}: {{.CPUPerc}}: {{.MemUsage}}: {{.MemPerc}}: {{.NetIO}}: {{.BlockIO}}" | ./bin/kafka-console-producer.sh --broker-list sparknode19:9092,sparknode20:9092,gas:9092 --topic $topic

#!/bin/bash
period=6 #Default period : ie 1 sec
period=$1
ssh -t  sparknode18@10.21.239.161  "echo boss |sudo -S  . ./opt/kafka_2.11-0.9.0.0/read_dockerstats.sh 6 | /opt/kafka_2.11-0.9.0.0/bin/kafka-console-producer.sh --broker-list sparknode19:9092,sparknode18:9092,gas:9092 --topic mach2"
ssh  -t  sparknode19@10.21.235.8 "echo boss |sudo -S . /opt/kafka_2.11-0.9.0.0/read_dockerstats.sh 6 | /opt/kafka_2.11-0.9.0.0/bin/kafka-console-producer.sh --broker-list sparknode19:9092,sparknode18:9092,gas:9092 --topic mach1"
ssh -t gas@10.21.229.203 "echo manipraju | sudo -S . /opt/kafka_2.11-0.9.0.0/read_dockerstats.sh 6  | /opt/kafka_2.11-0.9.0.0/bin/kafka-console-producer.sh --broker-list sparknode19:9092,sparknode18:9092,gas:9092 --topic mach3" 




