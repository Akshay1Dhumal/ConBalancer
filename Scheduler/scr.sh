#!/bin/bash

#docker checkpoint create --checkpoint-dir=/opt/kafka_2.11-0.9.0.0/checkpoint t1 c1
#docker container export --output="t1.tar" t1

#cat t1.tar  |docker import --change "CMD /bin/bash" -  tomcat3:new
#docker create --name t2 tomcat2:new
ssh -t $1 "ls /opt/; docker images"
 
