#!/bin/bash
#Machine username of zookeeprr and kafka nodes
user_name=(sparknode17 sparknode17)
user_ips=(10.21.235.181 10.21.233.193)
at_rate="@"

echo "STOPPING zookeeper and kafka on all nodes"
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


#StOPPING KAFKA on all machines
for i  in "${!mach_name[@]}"
do	
	echo "Stopping kafka servers on "${mach_name[i]}
        ssh -t ${mach_name[i]} " echo boss | sudo -S /opt/kafka_2.11-0.9.0.0/stop_kafka.sh" &
	#ssh -t ${mach_name[i]} "pid=$(ps aux | grep 'kafka.Kafka' | awk '{print $2}' | head -1); echo $pid |xargs kill -s -SIGTERM"
done

sleep 5


echo  "Hello killing zk"
#Stopping zookeeper on all machines

for i  in "${!mach_name[@]}"
do
	echo "Stopping zookeeper server on "${mach_name[i]}
        ssh -t ${mach_name[i]}  "echo boss | sudo - S /opt/kafka_2.11-0.9.0.0/stop_zk.sh" & 
done
