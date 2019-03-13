
#./bin/zookeeper-server-start.sh config/zookeeper.properties &
#sleep 5
./bin/kafka-server-start.sh  --override delete.topic.enable=true config/server.properties &

