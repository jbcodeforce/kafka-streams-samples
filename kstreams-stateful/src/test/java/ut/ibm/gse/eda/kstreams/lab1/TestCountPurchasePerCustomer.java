package ut.ibm.gse.eda.kstreams.lab1;

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
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import ibm.gse.eda.domain.Purchase;
import ibm.gse.eda.domain.util.JSONSerde;

/**
 * Count the number of puchases done by customer, using the unique customer ID.
 * 
 * Uses branches for unknown customer id
 * 
 * Uses a state store to keep aggregates. ktable as storage to count number of purchase
 * state stores are fault-tolerant. In case of failure, Kafka Streams guarantees to fully 
 * restore all state stores prior to resuming the processing
 */
public class TestCountPurchasePerCustomer {

    private static TopologyTestDriver testDriver;
    private static String inTopicName = "purchases";
    private static String outTopicName = "output";
    private static String errorTopicName = "errors";
    private static String storeName = "purchaseCount";
    private static TestInputTopic<String, Purchase> inTopic;
    private static TestOutputTopic<String, Long> outTopic;
    private static TestOutputTopic<String, String> errorTopic;
    
    public static Properties getStreamsConfig() {
        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "kstream-lab3");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummmy:1234");
        return props;
    }
    
    @BeforeClass
    public static void buildTopology(){
        final StreamsBuilder builder = new StreamsBuilder();
        KStream<String,Purchase> purchaseStream = 
                builder.stream(
                    inTopicName, 
                    Consumed.with(Serdes.String(), new JSONSerde<Purchase>())
                );

        // first verify customer id is present, if not route to error
        KStream<String,Purchase>[] branches = 
                purchaseStream.branch(
                    (k,v) -> v.getCustomerId() == null,
                    (k,v) -> true
                );
        // handle error
        branches[0].map(
                 (k, v) -> { return KeyValue.pair(k,"No customer id provided");}
                 )
                .to(
                    errorTopicName, Produced.with(Serdes.String(), Serdes.String())
                );

        // use groupBy to swap the key, then count by customer id,

        branches[1].groupBy(
                (k,v) -> v.getCustomerId()
                )
                // change the stream type from KGroupedStream to KTable<String, Long>.
                .count(
                    Materialized.as(storeName)
                )
                .toStream()
                .to(
                    outTopicName, Produced.with(Serdes.String(), Serdes.Long())
                );
        /* could also have done:
        branches[1].map( (k,v) -> {
            return KeyValue.pair(v.getCustomerId(),v);
            })
            .groupByKey()
        */
        testDriver = new TopologyTestDriver(builder.build(), getStreamsConfig());
        inTopic = testDriver.createInputTopic(inTopicName, new StringSerializer(), new JSONSerde<Purchase>());
        outTopic = testDriver.createOutputTopic(outTopicName,new StringDeserializer(), new LongDeserializer());
        //outTopic = testDriver.createOutputTopic(outTopicName,new StringDeserializer(), new JSONSerde<Purchase>());
        errorTopic = testDriver.createOutputTopic(errorTopicName, new StringDeserializer(), new StringDeserializer());
    }

    @Test
    public void shouldHaveOnePurchase(){
        Purchase p = new Purchase();
        p.setId( "p01");
        p.setCustomerId("CUST-01");
        p.setItemPurchased("product-01");
        inTopic.pipeInput(p.getId(),p);
        Assert.assertTrue( ! outTopic.isEmpty()); 
        System.out.println(outTopic.readKeyValue());
    }

    @Test
    public void shouldHaveAnError(){
        Purchase p = new Purchase();
        p.setId( "p02");
        p.setItemPurchased("product-01");
        inTopic.pipeInput(p.getId(),p);
        Assert.assertTrue( outTopic.isEmpty()); 
        Assert.assertTrue( ! errorTopic.isEmpty()); 
        System.out.println(errorTopic.readKeyValue());
    }
}