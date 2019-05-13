javac -cp .:/opt/kafka_2.11-0.9.0.0/libs/* /opt/kafka_2.11-0.9.0.0/ClientListener.java 
echo $@
java -cp /opt/kafka_2.11-0.9.0.0/.:/opt/kafka_2.11-0.9.0.0/libs/* ClientListener $@

