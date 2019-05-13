#!/bin/bash
#Machine username and IP set here

IFS=$'\n' read -d '' -r -a user_name < user_name
IFS=$'\n' read -d '' -r -a user_ips < user_ips




#user_name=(sparknode17 sparknode17 sparknode19 sparknode18 sparknode18)
#user_ips=(10.21.235.181 10.21.233.193 10.21.234.54 10.21.239.216 10.21.239.161)
at_rate="@"
#topic_m="M"


#period=$1
#period=6 #Default period : ie 1 sec
#echo "Period is "$period
#ZOOKEEPER IPS SET HERE
ips_z=(10.21.235.181 10.21.233.193)
#KAFKA-BROKERS SET HERE
ips_b=(10.21.235.181 10.21.233.193)

for i in "${!user_name[@]}"
do
        mach_name[$i]=${user_name[i]}$at_rate${user_ips[i]}
        #echo ${mach_name[i]}
done



for i  in "${!mach_name[@]}"
do
        #topic=$topic_m$i
        echo "Machine and Topic is " ${mach_name[i]} $topic
        #echo "Machine and Topic is " ${mach_name[i]} $topic >> fname
        ssh   ${mach_name[i]}  "echo boss | /opt/kafka_2.11-0.9.0.0/run.sh" &
done
#sleep 10s

#x=$(/opt/kafka_2.11-0.9.0.0/start_producers.sh);
#x=$(/opt/kafka_2.11-0.9.0.0/start_listeners.sh);


