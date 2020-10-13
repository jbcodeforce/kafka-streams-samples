# Kafka Streams Samples

This repository regroups a set of personal studies and quick summary on Kafka Streams.

Updated date 9/17/2020

## Run Kafka 2.5 locally for development

The docker compose file, under `local-cluster` starts one zookeeper and two Kafka brokers locally on the `kafkanet` network: `docker-compose up &`

To start `kafkacat` using the debezium tooling do the following:

```shell
docker run --tty --rm -i --network kafkanet debezium/tooling:latest
```

If you run with Event Streams on IBM Cloud set the KAFKA_BROKERS and KAFKA_USER and KAFKA_PWD environment variables accordingly (token and apikey) if you run on premise add the KAFKA_.

## Projects

Most of the Kafka streams examples in this repository are implemented as unit tests. So `mvn test` will run all of them.

### Getting started

The following samples are defined under the [kstreams-getting-started](https://github.com/jbcodeforce/kafka-streams-samples/tree/master/kstreams-getting-started) folder. Basically going under the `src/test/java` folder and go over the different test classes.

Streams topology could be tested outside of Kafka run time environment using the [TopologyTestDriver](https://kafka.apache.org/25/javadoc/org/apache/kafka/streams/TopologyTestDriver.html). Each test defines the following elements:

* a simple configuration for the test driver, with input and output topics
* a Kafka streams topology or pipeline to test
* a set of tests to define data to send to input topic and assertions on the expected results coming from the output topic.

#### Understanding the topology test driver

The **Lab 1** proposes to go over how to use TopologyTestDriver class: [base class](https://github.com/jbcodeforce/kafka-streams-samples/blob/master/kstreams-getting-started/src/test/java/ut/ibm/gse/eda/kstreams/lab1/TestKStreamTestDriverBase.java) and a second [more complex usage](https://github.com/jbcodeforce/kafka-streams-samples/blob/master/kstreams-getting-started/src/test/java/ut/ibm/gse/eda/kstreams/lab1/TestKStreamTestDriver.java) with clock wall and advance time to produce event with controlled time stamps

#### Transforming data

The common data transformation use cases can be easily  done with Kafka streams. The **lab2**: [sample](https://github.com/jbcodeforce/kafka-streams-samples/blob/master/kstreams-getting-started/src/test/java/ut/ibm/gse/eda/kstreams/lab2/EncryptCreditCardTest.java) is presenting how to encrypt an attribute from the input record. 

#### The integration test

`debezium` has a tool to run an Embedded kafka. The **lab3**: TO COMPLETE: use an embedded kafka to do tests and not the TopologyTestDriver, so it runs with QuarkusTest


*This project was created with `mvn io.quarkus:quarkus-maven-plugin:1.4.2.Final:create \
    -DprojectGroupId=ibm.gse.eda \
    -DprojectArtifactId=kstreams-getting-started \
    -DclassName="ibm.gse.eda.api.GreetingResource" \
    -Dpath="/hello"`* 

### Stateful stream examples

With Kafka streams we can do a lot of very interesting stateful processing using KTable, GlobalKTable, Windowing, aggregates... Those samples are under the [kstreams-stateful](https://github.com/jbcodeforce/kafka-streams-samples/tree/master/kstreams-stateful) folder.

The test folders includes a set of stateful test cases

* As part of lab 1, the test: [TestLoadKtableFromTopic.java](https://github.com/jbcodeforce/kafka-streams-samples/blob/master/kstreams-stateful/src/test/java/ut/ibm/gse/eda/kstreams/lab1/TestLoadKtableFromTopic.java) illustrates how to load reference data from a topic and use them as part of a KTable.
* As part of lab 1, the test: [TestCountPurchasePerCustomer](https://github.com/jbcodeforce/kafka-streams-samples/blob/master/kstreams-stateful/src/test/java/ut/ibm/gse/eda/kstreams/lab1/TestCountPurchasePerCustomer.java),  counts the number of puchases done by customer. It uses the unique customer ID as record key, and use kstreams branches to route unknown customer id to error topic, and uses a state store and ktable to keep the number of purchase. In case of failure, Kafka Streams guarantees to fully restore all state stores prior to resuming the processing
* In lab 2: the test [TestFilterDuplicateKey](https://github.com/jbcodeforce/kafka-streams-samples/blob/master/kstreams-stateful/src/test/java/ut/ibm/gse/eda/kstreams/lab2/TestFilterDuplicateKey.java) demonstrates how to remove duplicate key records within a time window. Transactions in the input stream may have duplicate key, as the source producer may generate duplicates. When the topic partitions will be defined with enough partition to support the workload, the records with the same key are in the same partition. Therefore a state store can be local to the stream processing and duplicate keys will be easy to identify. If we need to keep key within a timw period then a windowed state store is used.
* The [second interesting sample](https://github.com/jbcodeforce/kafka-streams-samples/blob/master/kstreams-stateful/src/test/java/ut/ibm/gse/eda/kstreams/lab2/TestSendAlertForNotEnoughEvent.java) in lab 2 is aiming to send an alert if we have less than 4 event per hour for a vessel positions. The process has to wait until we are sure we won't see any more events within the specified window and then generates alert. The implementation uses the **kstreams suppress api** to suppress intermediate events, and emit event when window is closed. This is the illustration of [KIP-328](https://cwiki.apache.org/confluence/display/KAFKA/KIP-328%3A+Ability+to+suppress+updates+for+KTables) 


#### Joining 3 streams with reference data to build a document

This demonstration highlights how to join 3 streams into one to support use cases like:

* data enrichement from reference data
* data transformation by merging data

This represents a classical use case of data pipeline with CDC generating events from three different tables:

- products reference data: new products are rarely added: one every quarter.
- shipments: includes static information on where to ship the ordered products
- shipmentReferences: includes detailed about the shipment routes, legs and costs 

and the goal is to build a shipmentEnriched object to be send to a data lake for at rest analytics. The report document that merge most of the attributes of the 3 streams.

This process is done in batch mode, but moving to a CDC -> streams -> data lake pipeline brings a lot of visibility to the shipment process and help to have a real time view of aggregated object, that can be used by new event driven services.

### Kafka with Quarkus reactive messaging

The [Quarkus Kafka Streams guide](https://quarkus.io/guides/kafka-streams) has an interesting example of:

* A producer to create event from a list using Flowable API, in a reactive way.
* A Streaming processing to aggregate value with KTable, state store and interactive queries

The producer code has an interesting way to generate reference values to a topic with microprofile reactive messaging: `stations` is a hash mpa, and using [java.util.collection.stream()](https://docs.oracle.com/javase/8/docs/api/java/util/Collection.html#stream--) to create a stream from the elements of a collection, and then use the [Java Stream API](https://docs.oracle.com/javase/8/docs/api/java/util/stream/Stream.html) to support the development of streaming pipelines: a operation chains to apply on the source of the stream. In the example below the collection of stations becomes a stream on which each record is transformed to a Kafka record, which are then regrouped in a list. The Flowable class is part of the [reactive messaging api](http://reactivex.io/) and supports asynchronous processing which combined with the @Outgoing annotation, produces messages to a kafka topic.

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

Channels are mapped to Kafka topics using the `application.properties` Quarkus configuration file.

The stream processing is in the aggregator class. But what is interesting also in this example is the use of `interactive queries` to access the underlying state store using a given key. The query can be exposed via a REST end point.

```java
 public Response getWeatherStationData(@PathParam("id") int id) {
        WeatherStationDataResult result = interactiveQueries.getWeatherStationData(id);
```

and the query:

```java
KafkaStreams streams;
//...
ReadOnlyKeyValueStore<Integer, Aggregation> store = streams.store(TopologyProducer.WEATHER_STATIONS_STORE, QueryableStoreTypes.keyValueStore());
Aggregation result = store.get(id);
```

To build and run:

```shell
# under producer folder
docker build -f src/main/docker/Dockerfile.jvm -t quarkstream/producer-jvm .
# under aggregator folder
docker build -f src/main/docker/Dockerfile.jvm -t quarkstream/aggregator-jvm .
# can be combined by using the docker compose
docker-compose up -d --build
# Run under quarkus-reactive-msg
docker-compose up
# Run kafkacat
docker run --tty --rm -i --network kafkanet debezium/tooling:1.0
$ kafkacat -b kafka1:9092 -C -o beginning -q -t temperatures-aggregated
```

To build and run natively

```
./mvnw clean package -f producer/pom.xml -Pnative -Dnative-image.container-runtime=docker
./mvnw clean package -f aggregator/pom.xml -Pnative -Dnative-image.container-runtime=docker
export QUARKUS_MODE=native
```

### Fault tolerance and scaling out

The load and state can be distributed amongst multiple application instances running the same pipeline. Each node will then contain a subset of the aggregation results, but Kafka Streams provides you with an API to obtain the information which node is hosting a given key.

The application can then either fetch the data directly from the other instance, or simply point the client to the location of that other node.

With distributed application, the code needs to retrieve all the metadata about the distributed store, with something like:

```java
public List<PipelineMetadata> getMetaData() {
    return streams.allMetadataForStore(TopologyProducer.WEATHER_STATIONS_STORE)
            .stream()
            .map(m -> new PipelineMetadata(
                    m.hostInfo().host() + ":" + m.hostInfo().port(),
                    m.topicPartitions()
                        .stream()
                        .map(TopicPartition::toString)
                        .collect(Collectors.toSet()))
            )
            .collect(Collectors.toList());
}
```


To demonstrate the kafka streams scaling:

```shell
# Scale aggregator to 3 instances
docker-compose up -d --scale aggregator=3
# Start httpie
docker run --tty --rm -i --network ks debezium/tooling:1.0
# The application exposes information about all the host names via REST:
http aggregator:8080/weather-stations/meta-data
# Retrieve the data from one of the three hosts shown in the response
http 61a1718cb941:8080/weather-stations/data/2
# when the station is not managed by the local host there is a rerouting done
HTTP/1.1 303 See Other
Content-Length: 0
Location: http://6150b945a68f:8080/weather-stations/data/2

# usigin the follow option on httpie
http --follow 61a1718cb941:8080/weather-stations/data/2
```

Adding the health dependency in the pom.xml:

```xml
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-smallrye-health</artifactId>
    </dependency>
```

and then a [simple health class]()

We can see quarkus-kafka-streams will automatically add, a readiness health check to validate that all topics declared in the quarkus.kafka-streams.topics property are created, and a liveness health check based on the Kafka Streams state.


## Further readings

* [Our Apache Kafka summary](https://ibm-cloud-architecture.github.io/refarch-eda/kafka/readme)
* [Kafka Producer development considerations](https://ibm-cloud-architecture.github.io/refarch-eda/kafka/producers)
* [Kafka Consumer development considerations](https://ibm-cloud-architecture.github.io/refarch-eda/kafka/consumers)
* [Our Kafka streams summary](https://ibm-cloud-architecture.github.io/refarch-eda/kafka/kafka-stream)
* [Kafka Streams’ Take on Watermarks and Triggers](https://www.confluent.io/blog/kafka-streams-take-on-watermarks-and-triggers/) what continuous refinement with operational parameters means
* [Windowed aggregations over successively increasing timed windows](https://cwiki.apache.org/confluence/display/KAFKA/Windowed+aggregations+over+successively+increasing+timed+windows)
* A Quarkus based code template for Kafka consumer: [quarkus-event-driven-consumer-microservice-template](https://github.com/jbcodeforce/quarkus-event-driven-consumer-microservice-template).