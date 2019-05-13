#!/bin/bash
ps ax | grep -i 'zookeeper'  | grep -v grep | awk '{print $1}'| xargs kill -s SIGKILL
#var=$(ps aux | grep -i 'zookeeper' | grep -v grep | awk '{print $1}')
#echo "Killing pid "$var
#echo boss | kill -SIGTERM $var
