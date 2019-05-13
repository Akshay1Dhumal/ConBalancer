ps aux | grep ClientListener | grep -v grep | awk '{print $2}'| xargs kill -SIGTERM

