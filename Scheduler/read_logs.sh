con_ids=($(docker ps -aq))
END=2
x=0
while [ $x -lt $END ]; 
do
        echo ${con_ids[x]}
        docker logs ${con_ids[x]}
        x=$(($x+1))
done


