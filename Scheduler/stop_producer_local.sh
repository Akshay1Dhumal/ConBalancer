ps ax | grep -i  'kafka.tools.ConsoleProducer' | grep -v grep | awk '{print $1}'| xargs kill -SIGTERM
