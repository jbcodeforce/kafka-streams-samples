# Kafka Streams Getting started

In this article we are presenting how to use the Kafka Streams API combined with Kafka event sourcing to implement different simple use cases.

The use cases are implemented inside the test folder.  Some of the domain classes are defined in the `src/java` folder. Streams topology are done in the unit test but could also be part of a service class to be used as an example running with kafka.

## Lab 1 Getting started to use TopologyTestDriver

The goal of this first example is to understand how to use the [Kafka TopologyTestDriver API]() to test any Kafka Streams topology. This sample is a simplest version of the Confluent example defined [here](https://docs.confluent.io/current/streams/developer-guide/test-streams.html).

Streams topology could be tested outside of Kafka run time environment using the TopologyTestDriver. Each tests define the following:

* a simple configuration for the test driver, input and output topics
* a topology to test
* a set of tests to define data to send to input topic and assertions from the output topic.

## mask credit card number

Using a simple kstream to change data on the fly, like for example encrypt a credit card number. Test is [EncryptCreditCardTest](). This is the first test to use the TopologyTestDriver class to run business logic outside of kafka. The class uses org.apache.kafka.common.serialization.Serdes and String serdesm and a JSON serdes for the domain class Purchase.

The other interesting approach is to use domain object with builder class and DSL to define the model:

```java
Purchase.builder(p).maskCreditCard().build()
```


## Joining 3 streams with reference data to build a document

This is a simple example of joining 3 sources of kafka streams to build a merged document, with some reference data loaded from a topic:

* The shipment status is a reference table and loaded inside a kafka topic: shipment-status
* The order includes items and customer id reference
* Customer is about the customer profile
* Products is about products inventory. 
* The outcome is an order report document that merge most of the attributes of the 3 streams.



## How streams flows are resilient?

Specifying the replicas factor at the topic level, with a cluster of kafka brokers, combine with transactional event produce, ensure to do not lose messages. The producer client code has the list of all the brokers to contact in case of failure and will try to connect to any broker in the list. 

## How to scale?



## Further readings

* [Kafka Streams’ Take on Watermarks and Triggers](https://www.confluent.io/blog/kafka-streams-take-on-watermarks-and-triggers/) what continuous refinement with operational parameters means: 