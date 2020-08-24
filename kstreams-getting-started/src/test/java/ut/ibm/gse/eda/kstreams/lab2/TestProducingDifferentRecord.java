package ut.ibm.gse.eda.kstreams.lab2;

import java.util.Properties;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ibm.gse.eda.domain.Inventory;
import ibm.gse.eda.domain.Item;
import io.quarkus.kafka.client.serialization.JsonbSerde;

public class TestProducingDifferentRecord {
    private static TopologyTestDriver testDriver;
    private static String inTopicName = "items";
    private static String resultTopicName = "inventory";
    private JsonbSerde<Item> itemSerde = new JsonbSerde<>(Item.class);
    private JsonbSerde<Inventory> inventorySerde = new JsonbSerde<>(Inventory.class);
    private Serde<String> stringSerde = new Serdes.StringSerde();
    private TestInputTopic<String, Item> inputTopic;
    private TestOutputTopic<String, Inventory> outputTopic;


    public  Properties getStreamsConfig() {
        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "item-example");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummmy:1234");
       // props.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
       // props.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,  Serdes.String().getClass().getName());
        return props;
    }


    /**
     * flatmap to produce zero to many records from one input record processed.
     * Applying a grouping or a join after flatMap will result in re-partitioning of the records. If possible use flatMapValues instead, which will not cause data re-partitioning.
     * 
     * map is used to create one record. 
     * mapValue keeps the same key
     */
    @BeforeEach
    public void setup() {
        final StreamsBuilder builder = new StreamsBuilder();
        builder.stream(inTopicName, Consumed.with(Serdes.String(), itemSerde))
        .mapValues( v ->  {
            Inventory ivt = new Inventory();
            ivt.itemID = v.itemCode;
            ivt.quantity = v.quantity;
            return   ivt;})
        .to(resultTopicName, Produced.with(Serdes.String(), inventorySerde));

        testDriver = new TopologyTestDriver(builder.build(), getStreamsConfig());
        inputTopic = testDriver.createInputTopic(inTopicName, stringSerde.serializer(),itemSerde.serializer());
        outputTopic = testDriver.createOutputTopic(resultTopicName, stringSerde.deserializer(), inventorySerde.deserializer());
    }

    @AfterEach
    public void tearDown() {
        try {
            testDriver.close();
        } catch (final Exception e) {
             System.out.println("Ignoring exception, test failing due this exception:" + e.getLocalizedMessage());
        } 
    }

    @Test
    public void shouldGetOneInventoryFromOneItem(){
        //given an item is sold in a store
        Item item = new Item("Store-1","Item-1",2,33.2);
        inputTopic.pipeInput(item.itemCode, item);
        Assertions.assertFalse(outputTopic.isEmpty()); 
        // the key is the item-id, the value is an inventory object
        Assertions.assertEquals("Item-1", outputTopic.readKeyValue().value.itemID);
    }
}