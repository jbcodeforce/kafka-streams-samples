package ut.ibm.gse.eda.kstreams.lab3;

import java.time.Instant;
import java.util.Properties;

import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Named;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ibm.gse.eda.domain.Purchase;
import ibm.gse.eda.domain.util.JSONSerde;

public class TestFilterDuplicateKey {
    
    private TopologyTestDriver testDriver;
    private static String inTopicName = "transactions";
    private static String outTopicName = "output";
    private static String storeName = "transactionCountStore";
    private TestInputTopic<String, Purchase> inTopic;
    private TestOutputTopic<String, Long> outTopic;

    public Properties getStreamsConfig() {
        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "kstream-lab3");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummmy:1234");
        return props;
    }


    @Before
    public void buildTopology(){
        final StreamsBuilder builder = new StreamsBuilder();
        KStream<String,Purchase> purchaseStream = builder.stream(inTopicName, Consumed.with(Serdes.String(),
        new JSONSerde<Purchase>()));
        KTable<String,Long> transactionKeyCount = purchaseStream.groupByKey().count();
        transactionKeyCount.toStream(Named.as(outTopicName)).to(outTopicName);

        //filter( (k, v) -> keyNotInStore(k)).to(outTopic);
        testDriver = new TopologyTestDriver(builder.build(), getStreamsConfig());
        inTopic = testDriver.createInputTopic(inTopicName, new StringSerializer(), new JSONSerde<Purchase>());
        outTopic = testDriver.createOutputTopic(outTopicName,new StringDeserializer(), new LongDeserializer());
    }

    @After
    public void close(){
        testDriver.close();
    }

    @Test
    public void shouldNotHaveDuplicate(){
        producePurchases("CUST",10);
        Assert.assertTrue( ! outTopic.isEmpty()); 
        System.out.println(outTopic.readKeyValue());
        System.out.println(outTopic.readKeyValue());
        System.out.println(outTopic.readKeyValue());
        //KeyValueStore store = testDriver.getStateStore(storeName);
        //store.close();
    }

    private void producePurchases(String keyPrefix,int purchaseCount) {
        for (int i =0; i< purchaseCount; i++) {
            Purchase p = new Purchase();
            p.setCustomerId(keyPrefix + "_" + i);
            p.setCreditCardNumber("1234-345678901"+i);
            p.setCreationTime(Instant.now().getEpochSecond());
            inTopic.pipeInput(p.getCustomerId(),p);
        }
        
    }
}