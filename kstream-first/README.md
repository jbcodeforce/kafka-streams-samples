# kstream-first Project

## Goal first program in kafka streams

Based on the [Apache Kafka Stream first code](https://kafka.apache.org/documentation/streams/#:~:text=Kafka%20Streams%20is%20a%20client,Kafka's%20server%2Dside%20cluster%20technology)

  
* Use maven [kafka-streams](https://mvnrepository.com/artifact/org.apache.kafka/kafka-streams) - added to pom
* Use [Quarkus main](https://quarkus.io/guides/command-mode-reference)

*This application does not deploy to k8s*

## Running the application in dev mode

* Start kafka with kafdrop: `docker compose up -d` under the ../local-cluster folder.
* Start a producer

```sh
docker exec -ti kafka1 bash
./kafka-console-producer.sh --bootstrap-server localhost:9092 --topic TextLinesTopic
```

* Start a consumer

```
docker exec -ti kafka1 bash -c "bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 \
    --topic WordsWithCountsTopic \    
    --from-beginning \
    --formatter kafka.tools.DefaultMessageFormatter \
    --property print.key=true \
    --property print.value=true \
    --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
    --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer"
```

*You can run your application in dev mode that enables live coding using:

```shell script
quarkus dev
```

* Stop the kafka brokers

```sh
docker-compose down
```

## Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using: 
```shell script
./mvnw package -Pnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/kstream-first-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.html.
