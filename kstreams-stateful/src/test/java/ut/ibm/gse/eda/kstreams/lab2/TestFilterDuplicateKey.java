package ut.ibm.gse.eda.kstreams.lab2;

import java.time.Duration;
import java.time.Instant;
import java.util.Properties;

import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.state.Stores;
import org.apache.kafka.streams.state.WindowBytesStoreSupplier;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ibm.gse.eda.domain.Purchase;
import ibm.gse.eda.domain.util.JSONSerde;

/**
 * Transaction in the input stream may have duplicate key, as the source
 * producer may generate duplicates. 
 * When the topic partitions will be defined with enough partition to support the
 * workload, the records with the same key are in the same partition. Therefore
 * state store can be local to the stream processing and duplicate key will be easy
 * to identify. 
 * If we need to keep key in a period of time then a windowed state store can be used.
 * 
 * This example illustrates the windowed state store.
 */
public class TestFilterDuplicateKey {

    private static TopologyTestDriver testDriver;
    private static String inTopicName = "purchases";
    private static String outTopicName = "output";
    private static String storeName = "transactionCountStore";
    private static TestInputTopic<String, Purchase> inTopic;
    private static TestOutputTopic<String, Long> outTopic;
    private static Instant startTime;

    public static Properties getStreamsConfig() {
        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "kstream-lab3");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummmy:1234");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, JSONSerde.class);
        return props;
    }

    @BeforeAll
    public static void buildTopology() {
        final StreamsBuilder builder = new StreamsBuilder();
        TimeWindows everyFiveSeconds = TimeWindows.of(Duration.ofMillis(5000));
        startTime = Instant.parse("2020-01-03T10:15:30.00Z");
        // use a windowed store
        WindowBytesStoreSupplier storeSupplier = Stores.persistentTimestampedWindowStore(
                storeName,
                Duration.ofDays(1),
                Duration.ofMinutes(10),
                false);
        KStream<String,Purchase> purchaseStream = 
                builder.stream(
                    inTopicName, 
                    Consumed.with(Serdes.String(), new JSONSerde<Purchase>())
                );


        KTable<Windowed<String>, Long> transactionKeyCount = purchaseStream
            .groupByKey()
            // TimeWindowedKStream with key is Windowed type grouping orginial key and window id
            // and the result is written into a local windowed KeyValueStore
            .windowedBy(everyFiveSeconds)
            // New events are added to windows until their grace period ends
            .count( Materialized.as(storeSupplier));
        transactionKeyCount.toStream(Named.as(outTopicName))
            .filter( (k, v) -> v <= 1)
            .map( (windowedkey, v) -> new KeyValue<>(windowedkey.key(), v))
            .to(outTopicName, Produced.with(Serdes.String(), Serdes.Long()));

        // use a driver with wall clock time
        testDriver = new TopologyTestDriver(builder.build(), getStreamsConfig(),startTime);
        inTopic = testDriver.createInputTopic(inTopicName, new StringSerializer(), new JSONSerde<Purchase>());
        outTopic = testDriver.createOutputTopic(outTopicName, new StringDeserializer(), new LongDeserializer());
    }

    @AfterAll
    public static void close() {
        testDriver.close();
    }

    @Test
    public void shouldHaveRecordsWithOneKey() {
        System.out.println("Send 10 record with different key each");
        producePurchases("CUST", 10);
        Assertions.assertFalse(outTopic.isEmpty()); 
        for (int i = 0; i < 10; i++) {
            // each output record has one count
            Assertions.assertEquals(1, outTopic.readKeyValue().value);
        }
        Assertions.assertTrue(outTopic.isEmpty()); 
        
    }

    @Test
    public void shouldNotHaveDuplicate() {
        
        Purchase p = new Purchase();
        p.setId("ID_10");
        p.setCustomerId("CUST_01");
        System.out.println("Send one record with key ID_10");
        inTopic.pipeInput(p.getId(), p);
        System.out.println("Send 2nd record with key ID_10");
        inTopic.pipeInput(p.getId(), p);
        System.out.println("Send 3nd record with key ID_11");
        p.setId("ID_11");
        inTopic.pipeInput(p.getId(), p);
        Assertions.assertFalse(outTopic.isEmpty()); 
        // each output record has one count
        KeyValue<String,Long> record = outTopic.readKeyValue();
        Assertions.assertEquals("ID_10", record.key);
        System.out.println("got record count for key ID_10 " + record.value);
        Assertions.assertEquals("ID_11", outTopic.readKeyValue().key);
        System.out.println("got record count for key ID_11");
        // no more records as second record was filtered out
        Assertions.assertTrue(outTopic.isEmpty()); 
    }

    @Test
    public void shouldNotHaveDuplicateAsKeyAreNotInTimeWindow(){
        System.out.println("Send one record with key ID_20");
        Purchase p = new Purchase();
        p.setId("ID_20");
        p.setCustomerId("CUST_01");
        inTopic.pipeInput(p.getId(), p);
        System.out.println("Wait 10 s and send another record with key ID_20");
        inTopic.advanceTime(Duration.ofSeconds(10));
        inTopic.pipeInput(p.getId(), p);
        Assertions.assertFalse(outTopic.isEmpty()); 
        Assertions.assertEquals("ID_20", outTopic.readKeyValue().key);
        System.out.println("Got first record with key ID_20");
        Assertions.assertFalse(outTopic.isEmpty()); 
        Assertions.assertEquals("ID_20", outTopic.readKeyValue().key);
        System.out.println("Got second record with key ID_20 as it was published after the time window");
    }

    private void producePurchases(String keyPrefix, int purchaseCount) {
        for (int i = 0; i < purchaseCount; i++) {
            Purchase p = new Purchase();
            p.setId("ID_0" + i);
            p.setCustomerId(keyPrefix + "_" + i);
            p.setCreditCardNumber("1234-345678901" + i);
            p.setCreationTime(Instant.now().getEpochSecond());
            inTopic.pipeInput(p.getId(), p);
        }

    }
}