#id=5
#mkdir /var/zookeeper
#mkdir /var/zookeeper/data
#echo $id > /var/zookeeper/data/myid
fname=MyFile_"$(date +%Y%d%M%H%M%S)".dat
echo $fname
mach_name=(sparknode17@10.21.233.193 sparknode17@10.21.235.181)
for i  in "${!mach_name[@]}"
do
        ssh -t ${mach_name[i]}  "echo boss | sudo sh  '/opt/kafka_2.11-0.9.0.0/temp2.sh >>  /opt/kafka_2.11-0.9.0.0/$fname'"
done

