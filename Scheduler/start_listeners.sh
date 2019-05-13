#!/bin/bash
IFS=$'\n' read -d '' -r -a user_name < user_name
IFS=$'\n' read -d '' -r -a user_ips < user_ips


#user_name=(sparknode17 sparknode17 sparknode19 sparknode18 sparknode18)
#user_ips=(10.21.235.181 10.21.233.193 10.21.234.54 10.21.239.216 10.21.239.161)
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

