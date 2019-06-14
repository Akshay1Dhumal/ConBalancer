docker service create --name s3  --restart-condition none --replicas 5 deployable/stress:latest --cache 1  --timeout 120s  --metrics-brief
#docker service create --name s4 --restart-condition none --replicas 5 deployable/stress:latest --vm 1 --vm-bytes 50m -t 120s --metrics-brief
#docker service create --name s1 --restart-condition none --replicas 5  akshay1dhumal/iperf:latest 10.21.229.203 100 120
docker service create --name s1 --restart-condition none --replicas 5 deployable/stress:latest --cpu 1 --cpu-method queens --timeout 120s  --metrics-brief
#docker service create --name s2 --restart-condition none --replicas 5 deployable/stress:latest --stream 2  --timeout 120s  --metrics-brief
docker service create --name s2  --restart-condition none --replicas 5 deployable/stress:latest --cache 1  --timeout 120s  --metrics-brief
#docker service create --name s2 --restart-condition none --replicas 5  akshay1dhumal/iperf:latest 10.21.229.203 100 120
#docker service create --name s4 --restart-condition none --replicas 5 deployable/stress:latest --crypt 1   --timeout 120s  --metrics-brief
#docker service create --name s3  --restart-condition none --replicas 5 deployable/stress:latest --cache 1  --timeout 120s  --metrics-brief
#docker service create --name s4 --restart-condition none --replicas 5 deployable/stress:latest --cache 2 --timeout 120s  --metrics-brief
docker service create --name s4 --restart-condition none --replicas 5 deployable/stress:latest --cpu 1 --timeout 300s --cpu-method pi   --metrics-brief
#docker service create --name s4 --restart-condition none --replicas 5 deployable/stress:latest --cache 1 --timeout 300s  --metrics-brief
