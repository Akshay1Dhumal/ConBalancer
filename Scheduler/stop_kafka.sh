#!/bin/bash
ps ax | grep -i 'kafka.Kafka'  | grep -v grep | awk '{print $1}'| xargs kill -s SIGTERM
#echo "Killing pid "$var
#x=$(echo boss | sudo kill -SIGTERM $var)
