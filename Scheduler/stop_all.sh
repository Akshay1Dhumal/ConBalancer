#./bin/kafka-server-stop.sh config/server.properties &
#sleep 10
#./bin/zookeeper-server-stop.sh config/zookeeper.properties & 
ps ax | grep -i 'kafka\.Kafka' | grep java | grep -v grep | awk '{print $1}' | xargs kill -SIGTERM
sleep 1
ps ax | grep -i 'zookeeper' | grep -v grep | awk '{print $1}' | xargs kill -SIGKILL


#For removinfg data use the delete.sh file
