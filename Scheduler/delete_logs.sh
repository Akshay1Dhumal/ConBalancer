ps ax | grep -i 'kafka\.Kafka' | grep java | grep -v grep | awk '{print $1}' | xargs kill -SIGTERM
sleep 10
ps ax | grep -i 'zookeeper' | grep -v grep | awk '{print $1}' | xargs kill -SIGKILL

sleep 10

cd /data/kafka-logs/
rm -rf *
cd /opt/kafka_2.11-0.9.0.0/logs/
rm -rf *
cd /var/zookeeper/data/version-2/
rm -rf *
