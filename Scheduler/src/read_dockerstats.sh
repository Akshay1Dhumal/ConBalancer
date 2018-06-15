#!/bin/bash
period=2 #Default period : ie 1 sec
period=$1
echo "Period of stats reporting : $period" 
count=0
MemTot=$(cat /proc/meminfo | grep MemTotal | awk '{print $2}')
CpuTotal=$(grep -c ^processor /proc/cpuinfo)
sockets=$(lscpu | grep -E 'Socket' | awk '{print $2}')

CON="CONTAINER"

docker stats --format "table {{.Container}}: {{.CPUPerc}}: {{.MemUsage}}: {{.MemPerc}}: {{.NetIO}}: {{.BlockIO}}" | while read line; do
#docker stats | while read line; do
set -- $line

if [[ "$1" ==  *"$CON"* ]]; then
count=$((count + 1))
else

if [ `expr $count % $period` -eq 0 ];then
MemAvail=$(cat /proc/meminfo | grep MemAvail | awk '{print $2}')


# go to each contaienr and fetch additional informationp
#pushd /sys/fs/cgroup/memory/docker/ 
cd /sys/fs/cgroup/memory/docker/
for d in */ ; do

    #echo "folders $d"
    folder_name=$(echo $d | cut -c1-12)
    con_name=$(echo $1 | cut -c1-12)
    #echo $folder_name $con_name        
    if [[ "$folder_name" == "$con_name" ]];then
    cd /sys/fs/cgroup/memory/docker/$d
    mem_numa=$(cat memory.numa_stat | head -1 | grep total)
    #echo $mem_numa "MEMORY NUMA................................."

    fi

done

#echo "$MemTot $MemAvail $CpuTotal $sockets"
echo "$line : $MemTot : $MemAvail : $CpuTotal : $sockets : $mem_numa :  " 
fi
fi
done

