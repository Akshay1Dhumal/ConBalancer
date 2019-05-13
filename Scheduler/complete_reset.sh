#!/bin/bash
user_name=(sparknode17 sparknode17)
user_ips=(10.21.235.181 10.21.233.193)

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
        ssh -t ${mach_name[i]}  "echo boss | ./opt/kafka_2.11-0.9.0.0/stop_kafka.sh"
done

sleep 5


#Stopping zookeeper on all machines

for i  in "${!mach_name[@]}"
do
        ssh -t ${mach_name[i]}  "echo boss | ./opt/kafka_2.11-0.9.0.0/stop_zk.sh"
done


#Removing logs  on all machines
for i  in "${!mach_name[@]}"
do
        ssh -t ${mach_name[i]}  "echo boss | ./opt/kafka_2.11-0.9.0.0/delete_logs.sh"
done

sleep 5


