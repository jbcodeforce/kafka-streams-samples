# Apache Kafka Streams Sampless

This repository regroup a set of personal studies and quick summary on Kafka.

## Run Kafka 2.5 locally for development

The docker compose starts one zookeeper and two kafka brokers locally on the `kafkanet` network.

To start `kafkacat` using the debezium tooling

```shell
docker run --tty --rm -i --network kafkanet debezium/tooling:latest
```

If you run with Event Streams on Cloud set the KAFKA_BROKERS and KAFKA_APIKEY environment variables accordingly.

## Kafka Streams Samples

### kstreams-getting started

In the `kstreams-getting-started` we use the TopologyTestDriver to implement a set of simple labs from simple topology to more complex solution. Basically going under the `src/test/java` folder and go lab by lab should be sufficient. 

*This project was created with `mvn archetype:generate -DgroupId=ibm.gse.eda -DartifactId=kstreams-getting-started -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.4 -DinteractiveMode=false`* 

To test the different labs run `mvn test`.

Some summary of what the labs do and some of the concepts and reference to material sources is in [this note](kstreams.md).

### Kstreams stateful

The `Kstreams-stateful` folder includes tests for working with ktables, store, aggregates... with unit tests and integration tests.

### Joining streams with time windows

The `streams-joining-sample` includes a simple java based Open Liberty app to listen to 4 streams of data to join within a time windows.


### Kafka with Quarkus reactive messaging

The `Quarkus reactive msg` folder includes a clone of the code from , slighlty adapted.
` ./mvnw compile quarkus:dev` to start local quarkus app and continuously develop.

Here is a template code for quarkus based Kafka consumer: [quarkus-event-driven-consumer-microservice-template](https://github.com/jbcodeforce/quarkus-event-driven-consumer-microservice-template).

Read this interesting guide with Quarkus and kafka streaming: [Quarkus using Kafka Streams](https://quarkus.io/guides/kafka-streams), which is implemented in the quarkus-reactive-msg producer, aggregator folders.

To generate the starting code for the producer we use the quarkus maven plugin with kafka extension:
`mvn io.quarkus:quarkus-maven-plugin:1.4.1.Final:create -DprojectGroupId=jbcodeforce.kafka.study -DprojectArtifactId=producer -Dextensions="kafka"`

for the aggregator:

`mvn io.quarkus:quarkus-maven-plugin:1.4.1.Final:create -DprojectGroupId=jbcodeforce.kafka.study -DprojectArtifactId=aggregator -Dextensions="kafka-streams,resteasy-jsonb"`

Interesting how to generate reference value to a topic with microprofile reactive messaging. `stations` is a hash:

```java
    @Outgoing("weather-stations")                               
    public Flowable<KafkaRecord<Integer, String>> weatherStations() {
        List<KafkaRecord<Integer, String>> stationsAsJson = stations.stream()
            .map(s -> KafkaRecord.of(
                    s.id,
                    "{ \"id\" : " + s.id +
                    ", \"name\" : \"" + s.name + "\" }"))
            .collect(Collectors.toList());

        return Flowable.fromIterable(stationsAsJson);
    };
```

Channels are mapped to Kafka topics using the Quarkus configuration file `application.properties`.

To build and run:

```shell
# under producer folder
docker build -f src/main/docker/Dockerfile.jvm -t quarkstream/producer-jvm .
# under aggregator folder
docker build -f src/main/docker/Dockerfile.jvm -t quarkstream/aggregator-jvm .
# Run under quarkus-reactive-msg
docker-compose up
# Run kafkacat
docker run --tty --rm -i --network kafkanet debezium/tooling:1.0
$ kafkacat -b kafka1:9092 -C -o beginning -q -t temperatures-aggregated
```
