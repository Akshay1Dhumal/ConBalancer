docker run -d deployable/stress:latest --cpu 2 --timeout 300s  --cpu-method matrixprod --metrics-brief &
docker run -d deployable/stress:latest --cpu 2 --timeout 300s --cpu-method matrixprod --metrics-brief & 
#docker run -d deployable/stress:latest --cache 2 --timeout 30s  --metrics-brief &

#docker run -d akshay1dhumal/iperf:latest 10.21.229.203 500 120 &
#docker run -d akshay1dhumal/iperf:latest 10.21.237.195 500 120 &



#Readahead

#docker run -d deployable/stress:latest --readahead 2 --readahead-bytes 50m  --metrics-brief --timeout 300s &
#docker run -d deployable/stress:latest --readahead 2 --readahead-bytes 50m  --metrics-brief --timeout 300s &


#CRyptiography
#docker run -d deployable/stress:latest --crypt 2 --timeout 120s --metrics-brief &
#docker run -d deployable/stress:latest --crypt 2 --timeout 120s --metrics-brief &


#bsearch
#docker run -d deployable/stress:latest --bsearch 2 --bsearch-size 4m --timeout 120s --metrics-brief &
#docker run -d deployable/stress:latest --bsearch 2 --bsearch-size 4m --timeout 120s --metrics-brief &


#NETWROK IPERF CLIENT 1gbps: server : 10.21.229.203 (MY system)

#docker run -d  oneclient:latest 10.21.229.203 500 20 &
#docker run -d  oneclient:latest 10.21.237.195 500 20 &



#VM STRESSOR
#docker run -d deployable/stress:latest --vm 2 --vm-bytes 512m -t 2m --metrics-brief &
#docker run -d deployable/stress:latest --vm 2 --vm-bytes 512m -t 2m --metrics-brief &


#TSEARCH
#docker run -d deployable/stress:latest --tsearch 2 --tsearch-size 4m --timeout 300s --metrics-brief &
#docker run -d deployable/stress:latest --tsearch 2 --tsearch-size 4m --timeout 300s --metrics-brief &

#qsort
#docker run -d deployable/stress:latest --qsort 2 --qsort-size 4m --timeout 300s --metrics-brief &
#docker run -d deployable/stress:latest --qsort 2 --qsort-size 4m --timeout 300s --metrics-brief &


