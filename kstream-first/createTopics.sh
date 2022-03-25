echo "######################"
echo " create Topics"


docker  exec -ti kafka1 bash -c "/opt/kafka/bin/kafka-topics.sh --bootstrap-server kafka1:29092 --create  --replication-factor 1 --partitions 1 --topic TextLinesTopic"

docker  exec -ti kafka1 bash -c "/opt/kafka/bin/kafka-topics.sh --bootstrap-server kafka1:29092 --create  --replication-factor 1 --partitions 1 --topic WordsWithCountsTopic"

