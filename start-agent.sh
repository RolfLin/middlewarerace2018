#!/bin/bash

ETCD_HOST=etcd
ETCD_PORT=2379
ETCD_URL=http://$ETCD_HOST:$ETCD_PORT

echo ETCD_URL = $ETCD_URL

echo start dstat log to file /root/logs/dstat.log ...
dstat --noheaders --nocolor -tcyns --tcp --socket > /root/logs/dstat.log &

echo start top log to file /root/logs/top.log ...
top -b > /root/logs/top.log &


if [[ "$1" == "consumer" ]]; then
  echo "Starting consumer agent..."
  java -jar \
       -Xms440M \
       -Xmx440M \
       -Dtype=consumer \
       -Dserver.port=0 \
       -Dnetty.server.port=20000\
       -Ddubbo.protocol.port=20880 \
       -Dendpoint.num=3 \
       -Detcd.url=$ETCD_URL \
       -Dlogs.dir=/root/logs \
       -Dio.netty.leakDetection.level=advanced \
       -XX:+PrintGCDetails \
       -XX:+PrintGCTimeStamps \
       /root/dists/mesh-agent.jar
elif [[ "$1" == "provider-small" ]]; then
  echo "Starting small provider agent..."
  java -jar \
       -Xms220M \
       -Xmx220M \
       -Dtype=provider \
       -Dsize=small \
       -Dserver.port=0 \
       -Dprovider.netty.server.port=30000\
       -Ddubbo.protocol.port=20880 \
       -Detcd.url=$ETCD_URL \
       -Dlogs.dir=/root/logs \
       -Dio.netty.leakDetection.level=advanced \
       -XX:+PrintGCDetails \
       -XX:+PrintGCTimeStamps \
       /root/dists/mesh-agent.jar
elif [[ "$1" == "provider-medium" ]]; then
  echo "Starting medium provider agent..."
  java -jar \
       -Xms440M \
       -Xmx440M \
       -Dtype=provider \
       -Dsize=medium \
       -Dserver.port=0 \
       -Dprovider.netty.server.port=30000\
       -Ddubbo.protocol.port=20880 \
       -Detcd.url=$ETCD_URL \
       -Dlogs.dir=/root/logs \
       -Dio.netty.leakDetection.level=advanced \
       -XX:+PrintGCDetails \
       -XX:+PrintGCTimeStamps \
       /root/dists/mesh-agent.jar
elif [[ "$1" == "provider-large" ]]; then
  echo "Starting large provider agent..."
  java -jar \
       -Xms660M \
       -Xmx660M \
       -Dtype=provider \
       -Dsize=large \
       -Dserver.port=0 \
       -Dprovider.netty.server.port=30000\
       -Ddubbo.protocol.port=20880 \
       -Detcd.url=$ETCD_URL \
       -Dlogs.dir=/root/logs \
       -Dio.netty.leakDetection.level=advanced \
       -XX:+PrintGCDetails \
       -XX:+PrintGCTimeStamps \
       /root/dists/mesh-agent.jar
else
  echo "Unrecognized arguments, exit."
  exit 1
fi
