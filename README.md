# Kafka Streams Samples

Better read in [book format.](https://jbcodeforce.github.io/kafka-streams-samples/).

Update date 5/6/2020

## Projects

All the examples are modelized as unit tests. So `mvn test` will run all of them.

### Getting started

The following samples are defined under the [kstreams-getting-started](https://github.com/jbcodeforce/kafka-streams-samples/tree/master/kstreams-getting-started) folder.

* **Lab 1**: understand how to use TopologyTestDriver class: [base class](https://github.com/jbcodeforce/kafka-streams-samples/blob/master/kstreams-getting-started/src/test/java/ut/ibm/gse/eda/kstreams/lab1/TestKStreamTestDriverBase.java) and a second [more complex usage](https://github.com/jbcodeforce/kafka-streams-samples/blob/master/kstreams-getting-started/src/test/java/ut/ibm/gse/eda/kstreams/lab1/TestKStreamTestDriver.java) with clock wall and advance time to produce event with controlled time stamps
* **lab2**: [the first sample](https://github.com/jbcodeforce/kafka-streams-samples/blob/master/kstreams-getting-started/src/test/java/ut/ibm/gse/eda/kstreams/lab2/EncryptCreditCardTest.java) is about how to encrypt an attribute with the record payload. It is an illustration of stream data transformation use case.
* **lab3**: TO COMPLETE: use an embedded kafka to do tests

### Stateful stream examples

With Kafka streams we can do a lot of very interesting stateful processing using KTable, GlobalKTable, Windowing, aggregates... Those samples are under the [kstreams-stateful](https://github.com/jbcodeforce/kafka-streams-samples/tree/master/kstreams-stateful) folder.

The test folders includes a set of stateful test cases

* As part of lab 1, the test: [TestLoadKtableFromTopic.java](https://github.com/jbcodeforce/kafka-streams-samples/blob/master/kstreams-stateful/src/test/java/ut/ibm/gse/eda/kstreams/lab1/TestLoadKtableFromTopic.java) illustrates how to load reference data from a topic and use them as part of a KTable.
* As part of lab 1, the test: [TestCountPurchasePerCustomer](https://github.com/jbcodeforce/kafka-streams-samples/blob/master/kstreams-stateful/src/test/java/ut/ibm/gse/eda/kstreams/lab1/TestCountPurchasePerCustomer.java),  counts the number of puchases done by customer. It uses the unique customer ID as record key, and use kstreams branches to route unknown customer id to error topic, and uses a state store and ktable to keep the number of purchase. In case of failure, Kafka Streams guarantees to fully restore all state stores prior to resuming the processing
* In lab 2: the test [TestFilterDuplicateKey](https://github.com/jbcodeforce/kafka-streams-samples/blob/master/kstreams-stateful/src/test/java/ut/ibm/gse/eda/kstreams/lab2/TestFilterDuplicateKey.java) demonstrates how to remove duplicate key records within a time window. Transactions in the input stream may have duplicate key, as the source producer may generate duplicates. When the topic partitions will be defined with enough partition to support the workload, the records with the same key are in the same partition. Therefore a state store can be local to the stream processing and duplicate keys will be easy to identify. If we need to keep key within a timw period then a windowed state store is used.
* The [second interesting sample](https://github.com/jbcodeforce/kafka-streams-samples/blob/master/kstreams-stateful/src/test/java/ut/ibm/gse/eda/kstreams/lab2/TestSendAlertForNotEnoughEvent.java) in lab 2 is aiming to send an alert if we have less than 4 event per hour for a vessel positions. The process has to wait until we are sure we won't see any more events within the specified window and then generates alert. The implementation uses the **kstreams suppress api** to suppress intermediate events, and emit event when window is closed. This is the illustration of [KIP-328](https://cwiki.apache.org/confluence/display/KAFKA/KIP-328%3A+Ability+to+suppress+updates+for+KTables) 
 
The application itself is a web app, to do some fraud detection sample using Quarkus and reactive messaging on top of kafka topics.


### Quarkus reactive messaging

The folder [/quarkus-reactive-msg](https://github.com/jbcodeforce/kafka-streams-samples/tree/master/quarkus-reactive-msg) is a fork of the quarkus guide for streaming and reactive messaging integration.